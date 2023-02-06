package org.apache.camel.controller.test.processors;

import java.io.File;
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
    private Object deploymentDir = ConfigHolder.getInstance().get("analyzer.deployment.dir");
    private Object dataDir = ConfigHolder.getInstance().get("common.data.dir");

    @Override
    public void process(Exchange exchange) throws Exception {
        TestExecution testExecution = exchange.getMessage().getBody(TestExecution.class);
        assert testExecution != null;

        if (testExecution.getTestState().getStatus().equals(Constants.FAILED)) {
            LOG.info("Skipping analysis due to test failure");
            exchange.getMessage().setBody(testExecution);

            return;
        }

        CommandLine cmdLine = new CommandLine("java");

        if (testExecution.getCamelMeta().getBaselineVersion() != null) {
            cmdLine.addArgument("-Dbaseline.rate.file=${common.data.dir}/${tester}/${test.name}/${test.type}/${camel.baseline.version}.data");
            cmdLine.addArgument("-Dbaseline.latencies.file=${common.data.dir}/${tester}/${test.name}/${test.type}/${camel.baseline.version}.hdr");
        }

        cmdLine.addArgument("-Dtest.rate.file=${common.data.dir}/${tester}/${test.name}/${test.type}/${camel.version}.data");
        cmdLine.addArgument("-Dtest.latencies.file=${common.data.dir}/${tester}/${test.name}/${test.type}/${camel.version}.hdr");
        cmdLine.addArgument("-Dtest.name=${test.name}");
        cmdLine.addArgument("-Doutput.dir=${common.data.dir}/reports/${tester}/${test.name}/${test.type}");
        cmdLine.addArgument("-jar");
        cmdLine.addArgument("${analyzer.deployment.dir}/kafka-tester-analyzer-${camel.version}.jar");

        Map<String, Object> map = new HashMap<>();

        map.put("camel.version", testExecution.getCamelMeta().getCamelVersion());
        map.put("camel.baseline.version", testExecution.getCamelMeta().getBaselineVersion());
        map.put("analyzer.deployment.dir", deploymentDir);
        map.put("test.name", testExecution.getTestName());
        map.put("test.type", testExecution.getTestType());
        map.put("tester", testExecution.getTester());
        map.put("common.data.dir", dataDir);

        cmdLine.setSubstitutionMap(map);

        LOG.info("About to execute: {}", cmdLine);

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        int timeout = testExecution.getTimeout();
        if (timeout == 0) {
            timeout = 15;
        }

        ExecuteWatchdog watchdog = new ExecuteWatchdog(Duration.ofMinutes(timeout).toMillis());

        Executor executor = new DefaultExecutor();
        executor.setExitValue(1);
        executor.setWatchdog(watchdog);
        executor.execute(cmdLine, resultHandler);

        resultHandler.waitFor();

        final int exitValue = resultHandler.getExitValue();
        LOG.info("Finished with status: {}", exitValue);

        TestState testState = new TestState();

        testState.setState("finished");
        if (exitValue == 0) {
            testState.setStatus("success");
        } else {
            testState.setStatus("failed");
        }
        testExecution.setTestState(testState);


        final String reportDir = dataDir + File.separator + "reports" + File.separator + testExecution.getTester() + File.separator + testExecution.getTestName() + File.separator + testExecution.getTestType();
        ObjectMapper mapper = new ObjectMapper();
        if (testExecution.getCamelMeta().getBaselineVersion() != null) {
            final Path baselinePath = Path.of(reportDir, "baseline.json");
            LOG.info("Attaching baseline results at {} to the response", baselinePath);
            if (Files.exists(baselinePath)) {
                final String baseLine = Files.readString(baselinePath);

                testExecution.setBaselinedTestMetrics(mapper.readValue(baseLine, BaselinedTestMetrics.class));
            }
        } else {
            final Path resultsPath = Path.of(reportDir, "results.json");
            if (Files.exists(resultsPath)) {
                LOG.info("Attaching results at {} to the response", resultsPath);
                final String baseLine = Files.readString(resultsPath);

                testExecution.setTestMetrics(mapper.readValue(baseLine, TestMetrics.class));
            } else {
                LOG.warn("No result to attach to the response at either {} or {}", Path.of(reportDir, "results.json"),
                        Path.of(reportDir, "baseline.json"));
            }
        }

        exchange.getMessage().setBody(testExecution);

    }
}
