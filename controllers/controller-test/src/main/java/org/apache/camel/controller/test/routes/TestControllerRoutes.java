package org.apache.camel.controller.test.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.controller.common.types.TestExecution;
import org.apache.camel.controller.test.processors.NewTestProcessor;
import org.apache.camel.controller.test.processors.TestFinishedProcessor;
import org.apache.camel.model.dataformat.JsonLibrary;

public class TestControllerRoutes extends RouteBuilder {
    public static final String KAFKA_TEST_NEW_URI = "kafka:test.new";
    public static final String KAFKA_TEST_FINISHED_URI = "kafka:test.finished";
    public static final String KAFKA_ANALYSIS_FINISHED_URI = "kafka:analysis.finished";

    private NewTestProcessor newTestProcessor = new NewTestProcessor();
    private TestFinishedProcessor testFinishedProcessor = new TestFinishedProcessor();

    @Override
    public void configure() {
        from(KAFKA_TEST_NEW_URI)
                .routeId("test-new")
                .unmarshal().json(JsonLibrary.Jackson, TestExecution.class)
                .process(newTestProcessor::process)
                .marshal().json(JsonLibrary.Jackson)
                .to(KAFKA_TEST_FINISHED_URI);


        from(KAFKA_TEST_FINISHED_URI)
                .routeId("test-finished")
                .unmarshal().json(JsonLibrary.Jackson, TestExecution.class)
                .process(testFinishedProcessor::process)
                .marshal().json(JsonLibrary.Jackson)
                .to(KAFKA_ANALYSIS_FINISHED_URI);
    }
}
