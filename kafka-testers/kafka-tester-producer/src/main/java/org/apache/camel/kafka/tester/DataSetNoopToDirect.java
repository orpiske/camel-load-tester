package org.apache.camel.kafka.tester;

import java.io.File;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.kafka.tester.support.Sample;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Camel Java DSL Router
 */
public class DataSetNoopToDirect extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(DataSetNoopToDirect.class);
    private final int threadCount;

    private ProducerTemplate producerTemplate;

    public DataSetNoopToDirect(int threadCount) {
        this.threadCount = threadCount;


    }

    private void inject(Exchange exchange) {
        try {
            producerTemplate.sendBody("direct:test", Integer.valueOf(1));
            producerTemplate.sendBody("direct:test", "skip");
            producerTemplate.sendBody("direct:test", new File("a"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        LOG.info("Using thread count: {}", threadCount);

        producerTemplate = getContext().createProducerTemplate();

        from("dataset:testSet?produceDelay=0&minRate={{?min.rate}}&initialDelay={{initial.delay:2000}}&dataSetIndex=off")
                .unmarshal().json(JsonLibrary.Jackson, Sample.class)
                .routeId("dataset-noop")
                .process(this::inject)
//                .to("direct:test");
                .choice().when(body().contains("skip")).to("log: Skipped ${body}")
                .otherwise().to("direct:test");
    }
}
