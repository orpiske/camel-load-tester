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
public class DataSetNoopToSeda extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(DataSetNoopToSeda.class);

    private final int threadCount;
    private ProducerTemplate producerTemplate;

    public DataSetNoopToSeda(int threadCount) {
        this.threadCount = threadCount;
    }

    private void inject(Exchange exchange) {
        try {
            producerTemplate.sendBody("seda:test", Integer.valueOf(1));
            producerTemplate.sendBody("seda:test", "skip");
            producerTemplate.sendBody("seda:test", new File("a"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        producerTemplate = getContext().createProducerTemplate();

        onException(IllegalStateException.class)
                .process(e -> LOG.error("The SEDA queue is likely full and the system may be unable to catch to the load. Fix the test parameters"));

        LOG.info("Using thread count: {}", threadCount);

        from("dataset:testSet?produceDelay=0&minRate={{?min.rate}}&initialDelay={{initial.delay:2000}}&dataSetIndex=off")
                .routeId("dataset-noop-to-seda")
                .unmarshal().json(JsonLibrary.Jackson, Sample.class)
                .process(this::inject)
                .choice().when(body().contains("skip")).to("log: Skipped ${body}")
                .to("seda:test?blockWhenFull=true&offerTimeout=1000");
    }
}
