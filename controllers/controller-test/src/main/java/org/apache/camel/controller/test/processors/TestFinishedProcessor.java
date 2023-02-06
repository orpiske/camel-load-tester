package org.apache.camel.controller.test.processors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.controller.common.config.ConfigHolder;
import org.apache.camel.controller.common.types.Constants;
import org.apache.camel.controller.common.types.TestExecution;
import org.apache.camel.controller.common.types.TestState;
import org.apache.camel.kafka.tester.common.types.BaselinedTestMetrics;
import org.apache.camel.kafka.tester.common.types.TestMetrics;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestFinishedProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(TestFinishedProcessor.class);
    private final String deploymentDir = ConfigHolder.getInstance().getProperty("analyzer.deployment.dir");
    private final String dataDir = ConfigHolder.getInstance().getProperty("common.data.dir");

    @Override
    public void process(Exchange exchange) throws Exception {
        TestExecution testExecution = exchange.getMessage().getBody(TestExecution.class);
        assert testExecution != null;

        if (testExecution.getTestState().getStatus().equals(Constants.FAILED)) {
            LOG.info("Skipping analysis due to test failure");
            exchange.getMessage().setBody(testExecution);

            return;
        }

        CommandLine cmdLine = buildTestCommand(testExecution);

        final int exitValue = CommandUtil.executeCommand(testExecution, cmdLine);

        TestState testState = new TestState();

        testState.setState("finished");
        if (exitValue == 0) {
            testState.setStatus("success");
        } else {
            testState.setStatus("failed");
        }
        testExecution.setTestState(testState);


        attachMetrics(testExecution);

        exchange.getMessage().setBody(testExecution);

    }

    private CommandLine buildTestCommand(TestExecution testExecution) {
        CommandLine cmdLine = new CommandLine("java");


        if (testExecution.getCamelMeta().getBaselineVersion() != null) {
            cmdLine.addArgument("-Dbaseline.rate.file=${common.data.dir}/${tester}/${test.name}/${test.type}/${camel.baseline.version}.data");
            cmdLine.addArgument("-Dbaseline.latencies.file=${common.data.dir}/${tester}/${test.name}/${test.type}/${camel.baseline.version}.hdr");
            cmdLine.addArgument("-Doutput.dir=${common.data.dir}/reports/${tester}/${test.name}/${test.type}/${camel.version}/baselines/${camel.baseline.version}");
        } else {
            cmdLine.addArgument("-Doutput.dir=${common.data.dir}/reports/${tester}/${test.name}/${test.type}/${camel.version}");
        }

        cmdLine.addArgument("-Dtest.rate.file=${common.data.dir}/${tester}/${test.name}/${test.type}/${camel.version}.data");
        cmdLine.addArgument("-Dtest.latencies.file=${common.data.dir}/${tester}/${test.name}/${test.type}/${camel.version}.hdr");
        cmdLine.addArgument("-Dtest.name=${test.name}");

        cmdLine.addArgument("-jar");
        cmdLine.addArgument("${analyzer.deployment.dir}/kafka-tester-analyzer-${camel.version}.jar");

        Map<String, Object> map = new HashMap<>();

        map.put("common.data.dir", dataDir);
        map.put("tester", testExecution.getTester());
        map.put("test.name", testExecution.getTestName());
        map.put("test.type", testExecution.getTestType());
        map.put("camel.baseline.version", testExecution.getCamelMeta().getBaselineVersion());

        map.put("camel.version", testExecution.getCamelMeta().getCamelVersion());
        map.put("analyzer.deployment.dir", deploymentDir);

        cmdLine.setSubstitutionMap(map);

        LOG.info("About to execute: {}", cmdLine);
        return cmdLine;
    }

    private void attachMetrics(TestExecution testExecution) throws IOException {
        final Path basePath = Path.of(dataDir, "reports", testExecution.getTester(), testExecution.getTestName(), testExecution.getTestType(),
                testExecution.getCamelMeta().getCamelVersion());

        ObjectMapper mapper = new ObjectMapper();
        if (testExecution.getCamelMeta().getBaselineVersion() != null) {
            final Path baselinePath = Path.of(basePath.toString(), "baselines",
                        testExecution.getCamelMeta().getBaselineVersion(), "baseline.json");
            LOG.info("Attaching baseline results at {} to the response", baselinePath);

            if (Files.exists(baselinePath)) {
                final String baseLine = Files.readString(baselinePath);

                testExecution.setBaselinedTestMetrics(mapper.readValue(baseLine, BaselinedTestMetrics.class));
            } else {
                LOG.warn("There's no result file to attach to the response at {}", baselinePath);
            }
        } else {
            final Path resultsPath = Path.of(basePath.toString(), "results.json");
            if (Files.exists(resultsPath)) {
                LOG.info("Attaching results at {} to the response", resultsPath);
                final String baseLine = Files.readString(resultsPath);

                testExecution.setTestMetrics(mapper.readValue(baseLine, TestMetrics.class));
            } else {
                LOG.warn("There's no result file to attach to the response at {}", resultsPath);
            }
        }
    }
}
