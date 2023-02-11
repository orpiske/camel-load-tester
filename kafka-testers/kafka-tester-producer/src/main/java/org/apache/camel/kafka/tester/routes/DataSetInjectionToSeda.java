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
public class DataSetInjectionToSeda extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(DataSetInjectionToSeda.class);

    private final int threadCount;
    private ProducerTemplate producerTemplate;
    private Endpoint endpoint;

    public DataSetInjectionToSeda() {
        this.threadCount = Parameters.threadCount();
    }

    private void inject(Exchange exchange) {
        try {
            producerTemplate.sendBody(endpoint, Integer.valueOf(1));
            producerTemplate.sendBody(endpoint, "skip");
            producerTemplate.sendBody(endpoint, new File("a"));
            producerTemplate.send(endpoint, exchange);
        } catch (Exception e) {
            LOG.error("Error: {}", e.getMessage(), e.getMessage());
        }
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        producerTemplate = getContext().createProducerTemplate();
        endpoint = getContext().getEndpoint("seda:test?blockWhenFull=true&offerTimeout=1000");

        onException(IllegalStateException.class)
                .process(e -> LOG.error("The SEDA queue is likely full and the system may be unable to catch to the load. Fix the test parameters"));

        LOG.info("Using thread count: {}", threadCount);

        from("dataset:testSet?produceDelay=0&minRate={{?min.rate}}&initialDelay={{initial.delay:2000}}&dataSetIndex=off")
                .routeId("dataset-injection-to-seda")
                .unmarshal().json(JsonLibrary.Jackson, Sample.class)
                .process(this::inject)
                .choice()
                    .when(body().contains("skip")).to("log: Skipped ${body}")
                .otherwise().end();
    }
}
