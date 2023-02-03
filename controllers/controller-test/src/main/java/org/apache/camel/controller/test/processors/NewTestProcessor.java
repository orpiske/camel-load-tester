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

    @Override
    public void process(Exchange exchange) throws Exception {
        final Object deploymentDir = ConfigHolder.getInstance().get("producer.deployment.dir");

        TestExecution testExecution = exchange.getMessage().getBody(TestExecution.class);
        assert testExecution != null;

        // -Dcamel.main.durationMaxMessages=60000000
        // -Dtest.producer.type=noop
        // -javaagent:/Users/opiske/Projects/tmp/type-pollution-agent/agent/target/type-pollution-agent-0.1-SNAPSHOT.jar=org.apache.camel

        CommandLine cmdLine = new CommandLine("java");
        cmdLine.addArgument("-Dcamel.version=${camel.version}");
        cmdLine.addArgument("-Dcamel.main.durationMaxMessages=${camel.main.durationMaxMessages}");
        cmdLine.addArgument("-Dcamel.version=${camel.version}");
        cmdLine.addArgument("-Dcamel.component.kafka.brokers=${camel.component.kafka.brokers}");

        if (testExecution.getTester().equals("producer")) {
            cmdLine.addArgument("-Dtest.producer.type=${test.type}");
        } else {
            cmdLine.addArgument("-Dtest.consumer.type=${test.type}");
        }

        cmdLine.addArgument("-Dtest.file=${camel.version}.test");

        cmdLine.addArgument("-jar");
        if (testExecution.getTester().equals("producer")) {
            cmdLine.addArgument("${tester.deployment.dir}/kafka-tester-producer-${camel.version}.jar");
        } else {
            cmdLine.addArgument("${tester.deployment.dir}/kafka-tester-consumer-${camel.version}.jar");
        }

        Map<String, Object> map = new HashMap<>();

        map.put("camel.version", testExecution.getCamelMeta().getCamelVersion());
        map.put("camel.main.durationMaxMessages", testExecution.getTestDuration().getDurationValue());
        map.put("tester.deployment.dir", deploymentDir);
        map.put("camel.component.kafka.brokers", ConfigHolder.getInstance().get("camel.component.kafka.brokers"));
        map.put("test.type", testExecution.getTestType());

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
