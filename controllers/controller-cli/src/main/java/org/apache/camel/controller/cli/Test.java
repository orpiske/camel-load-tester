package org.apache.camel.controller.cli;

import java.util.UUID;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.kafka.KafkaComponent;
import org.apache.camel.controller.common.types.CamelMeta;
import org.apache.camel.controller.common.types.Header;
import org.apache.camel.controller.common.types.TestDuration;
import org.apache.camel.controller.common.types.TestExecution;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.kafka.tester.common.types.BaselinedTestMetrics;
import org.apache.camel.kafka.tester.common.types.TestMetrics;
import org.apache.camel.kafka.tester.common.types.TestResult;
import picocli.CommandLine;

@CommandLine.Command(name = "test", description = "Run a new performance test")
public class Test implements Callable<Integer> {

    @CommandLine.Option(names = {"-s", "--bootstrap-server"}, description = "The Kafka bootstrap server to use", required = true)
    private String bootstrapServer;

    @CommandLine.Option(names = {"--camel-version"}, description = "The Camel version under test", required = true)
    private String camelVersion;

    @CommandLine.Option(names = {"--camel-baseline-version"}, description = "The Camel version to compare to")
    private String camelBaselineVersion;

    @CommandLine.Option(names = {"--tester"}, description = "The tester to use (producer or consumer)", required = true)
    private String tester;

    @CommandLine.Option(names = {"--test-name"}, description = "The test name", required = true)
    private String testName;

    @CommandLine.Option(names = {"--test-type"}, description = "The test type (from the tester) to use (i.e.; noop, noop-seda-threaded, etc)", required = true)
    private String testType;

    @CommandLine.Option(names = {"--test-duration"}, description = "The test duration (max message)", required = true)
    private String durationMaxMessages;

    @CommandLine.Option(names = {"--test-timeout"}, defaultValue = "15", description = "The timeout (in minutes) for the test")
    private int timeout;

    @CommandLine.Option(names = {"--tester-arguments"}, description = "Additional arguments to pass to the tester")
    private String testerArguments;

    @CommandLine.Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
    private boolean helpRequested = false;

    @Override
    public Integer call() {
        try (CamelContext context = new DefaultCamelContext()) {

            context.getComponent("kafka", KafkaComponent.class).getConfiguration().setBrokers(bootstrapServer);
            context.start();

            final ProducerTemplate producerTemplate = context.createProducerTemplate();
            System.out.println("Executing the test");
            sendTestRequest(producerTemplate);

            System.out.println("Waiting for a response");
            final ConsumerTemplate consumerTemplate = context.createConsumerTemplate();
            if (waitForTestResult(consumerTemplate)) {
                System.out.println("Waiting for analysis");
                waitForAnalysisResult(consumerTemplate);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return 0;
    }

    private static boolean waitForTestResult(ConsumerTemplate consumerTemplate) throws JsonProcessingException {
        final Exchange completionExchange = consumerTemplate.receive("kafka:test.finished");

        String body = completionExchange.getMessage().getBody(String.class);

        ObjectMapper mapper = new ObjectMapper();
        final TestExecution testExecution = mapper.readValue(body, TestExecution.class);

        final String status = testExecution.getTestState().getStatus();
        System.out.println("Test executed and finished with status: " + status);

        if ("success".equals(status)) {
            return true;
        }

        return false;
    }

    private static void waitForAnalysisResult(ConsumerTemplate consumerTemplate) throws JsonProcessingException {
        final Exchange completionExchange = consumerTemplate.receive("kafka:analysis.finished");

        String body = completionExchange.getMessage().getBody(String.class);

        ObjectMapper mapper = new ObjectMapper();
        final TestExecution testExecution = mapper.readValue(body, TestExecution.class);
        BaselinedTestMetrics baselinedTestMetrics = testExecution.getBaselinedTestMetrics();

        if (baselinedTestMetrics != null) {

            System.out.println("Geo mean (baseline " + baselinedTestMetrics.getBaselineMetrics().getTestSuiteVersion()+ "): "
                    + baselinedTestMetrics.getBaselineMetrics().getMetrics().getGeoMean());
            System.out.println("Geo mean (test " + baselinedTestMetrics.getTestMetrics().getTestSuiteVersion() + "): "
                    + baselinedTestMetrics.getTestMetrics().getMetrics().getGeoMean());
        } else {
            TestMetrics testMetrics = testExecution.getTestMetrics();
            if (testMetrics != null) {
                System.out.println("Geo mean (test " + testMetrics.getTestSuiteVersion() + "): " +
                        testMetrics.getMetrics().getGeoMean());
            }
        }
    }

    private void sendTestRequest(ProducerTemplate producerTemplate) throws JsonProcessingException {
        TestExecution testExecution = new TestExecution();

        testExecution.setId(UUID.randomUUID().toString());

        Header header = new Header();

        header.setFormatVersion("1.0.0");
        testExecution.setHeader(header);

        CamelMeta camelMeta = new CamelMeta();
        camelMeta.setCamelVersion(camelVersion);
        camelMeta.setBaselineVersion(camelBaselineVersion);
        testExecution.setCamelMeta(camelMeta);

        TestDuration duration = new TestDuration();
        duration.setDurationType("max-messages");
        duration.setDurationValue(durationMaxMessages);
        testExecution.setTestDuration(duration);

        testExecution.setTester(tester);
        testExecution.setTestName(testName);
        testExecution.setTestType(testType);
        testExecution.setTesterArguments(testerArguments);
        testExecution.setTimeout(timeout);

        ObjectMapper mapper = new ObjectMapper();
        final String body = mapper.writeValueAsString(testExecution);

        producerTemplate.sendBody("kafka:test.new", body);
    }
}
