package org.apache.camel.load.tester.routes;

import org.apache.camel.builder.RouteBuilder;


/**
 * A Camel Java DSL Router
 */
public class DataSetNoopToDirect extends RouteBuilder {
    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        from("dataset:testSet?produceDelay=0&minRate={{?min.rate}}&initialDelay={{initial.delay:2000}}&dataSetIndex=off")
                .routeId("dataset-noop")
                .to("direct:test");
    }
}
