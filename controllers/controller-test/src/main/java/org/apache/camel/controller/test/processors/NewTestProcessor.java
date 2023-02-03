package org.apache.camel.controller.test.processors;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.controller.common.config.ConfigHolder;
import org.apache.camel.controller.common.types.TestExecution;
import org.apache.camel.controller.common.types.TestState;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewTestProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(NewTestProcessor.class);
    private Object dataDir = ConfigHolder.getInstance().get("common.data.dir");

    @Override
    public void process(Exchange exchange) throws Exception {
        TestExecution testExecution = exchange.getMessage().getBody(TestExecution.class);
        assert testExecution != null;

        CommandLine cmdLine = new CommandLine("java");
        cmdLine.addArgument("-Dcamel.version=${camel.version}");
        cmdLine.addArgument("-Dcamel.main.durationMaxMessages=${camel.main.durationMaxMessages}");
        cmdLine.addArgument("-Dcamel.version=${camel.version}");
        cmdLine.addArgument("-Dcamel.component.kafka.brokers=${camel.component.kafka.brokers}");

        cmdLine.addArgument("-Dtest.${tester}.type=${test.type}");
        cmdLine.addArgument("-Dtest.rate.file=${common.data.dir}/${tester}/${test.name}/${test.type}/${camel.version}.data");

        cmdLine.addArgument("-jar");
        cmdLine.addArgument("${tester.deployment.dir}/kafka-tester-${tester}-${camel.version}.jar");

        Map<String, Object> map = new HashMap<>();

        map.put("camel.version", testExecution.getCamelMeta().getCamelVersion());
        map.put("camel.main.durationMaxMessages", testExecution.getTestDuration().getDurationValue());

        if (testExecution.getTester().equals("producer")) {
            Object deploymentDir = ConfigHolder.getInstance().get("producer.deployment.dir");
            map.put("tester.deployment.dir", deploymentDir);
        } else {
            Object deploymentDir = ConfigHolder.getInstance().get("consumer.deployment.dir");
            map.put("tester.deployment.dir", deploymentDir);
        }

        map.put("camel.component.kafka.brokers", ConfigHolder.getInstance().get("camel.component.kafka.brokers"));
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

        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout * 1000);

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

        exchange.getMessage().setBody(testExecution);
    }
}