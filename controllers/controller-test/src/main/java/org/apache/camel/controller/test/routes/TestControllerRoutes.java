package org.apache.camel.controller.test.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.controller.common.types.TestExecution;
import org.apache.camel.controller.test.processors.NewTestProcessor;
import org.apache.camel.model.dataformat.JsonLibrary;

public class TestControllerRoutes extends RouteBuilder {
    private NewTestProcessor newTestProcessor = new NewTestProcessor();

    @Override
    public void configure() {
        from("kafka:test.new")
                .unmarshal().json(JsonLibrary.Jackson, TestExecution.class)
                .process(newTestProcessor::process)
                .marshal().json(JsonLibrary.Jackson)
                .to("kafka:test.finished");
    }
}
