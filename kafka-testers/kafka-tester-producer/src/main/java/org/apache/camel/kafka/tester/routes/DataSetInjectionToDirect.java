package org.apache.camel.kafka.tester.routes;

import java.io.File;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.kafka.tester.common.Parameters;
import org.apache.camel.kafka.tester.support.Sample;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Camel Java DSL Router
 */
public class DataSetInjectionToDirect extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(DataSetInjectionToDirect.class);
    private final int threadCount;

    private ProducerTemplate producerTemplate;
    private Endpoint endpoint;

    public DataSetInjectionToDirect() {
        this.threadCount = Parameters.threadCount();
    }

    private void inject(Exchange exchange) {
        try {
            producerTemplate.sendBody(endpoint, 1);
            producerTemplate.sendBody(endpoint, "skip");
            producerTemplate.sendBody(endpoint, new File("a"));
        } catch (Exception e) {
            LOG.error("Error: {}", e.getMessage(), e);
        }
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        LOG.info("Using thread count: {}", threadCount);

        producerTemplate = getContext().createProducerTemplate();
        endpoint = getContext().getEndpoint("direct:test");

        from("dataset:testSet?produceDelay=0&minRate={{?min.rate}}&initialDelay={{initial.delay:2000}}&dataSetIndex=off")
                .routeId("dataset-injection")
                .unmarshal().json(JsonLibrary.Jackson, Sample.class)
                .process(this::inject)
                .choice().when(body().contains("skip")).to("log: Skipped ${body}")
                .otherwise().to("direct:test");
    }
}
