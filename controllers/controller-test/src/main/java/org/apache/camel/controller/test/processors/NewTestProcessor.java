package org.apache.camel.controller.test.processors;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.controller.common.config.ConfigHolder;
import org.apache.camel.controller.common.types.TestExecution;
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

        CommandLine cmdLine = new CommandLine("java");
        cmdLine.addArgument("-Dcamel.version=${camel.version}");
        cmdLine.addArgument("-Dcamel.main.durationMaxMessages=${camel.main.durationMaxMessages}");
        cmdLine.addArgument("-Dcamel.version=${camel.version}");
        cmdLine.addArgument("-Dcamel.component.kafka.brokers=${camel.component.kafka.brokers}");

        cmdLine.addArgument("-Dtest.file=${camel.version}.test");

        cmdLine.addArgument("-jar");
        cmdLine.addArgument("${tester.deployment.dir}/kafka-tester-producer-${camel.version}.jar");

        Map<String, Object> map = new HashMap<>();

        map.put("camel.version", testExecution.getCamelMeta().getCamelVersion());
        map.put("camel.main.durationMaxMessages", testExecution.getTestDuration().getDurationValue());
        map.put("tester.deployment.dir", deploymentDir);
        map.put("camel.component.kafka.brokers", ConfigHolder.getInstance().get("camel.component.kafka.brokers"));

        cmdLine.setSubstitutionMap(map);

        LOG.info("About to execute: {}", cmdLine);

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        ExecuteWatchdog watchdog = new ExecuteWatchdog(60*1000);
        Executor executor = new DefaultExecutor();
        executor.setExitValue(1);
        executor.setWatchdog(watchdog);
        executor.execute(cmdLine, resultHandler);

        resultHandler.waitFor();

        LOG.info("Finished with status: {}", resultHandler.getExitValue());
    }
}
