package org.apache.camel.kafka.tester;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Camel Java DSL Router
 */
public class TestNoopDirectThreadedProducer extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(TestNoopDirectThreadedProducer.class);

    private final LongAdder longAdder;
    private final int batchSize;

    public TestNoopDirectThreadedProducer(LongAdder longAdder, int batchSize) {
        this.longAdder = longAdder;
        this.batchSize = batchSize;
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        from("dataset:testSet?produceDelay=0&minRate={{?min.rate}}&initialDelay={{initial.delay:2000}}&dataSetIndex=off")
                .to("direct:test");

        if (batchSize == 0) {
            from("direct:test")
                    .process(exchange -> longAdder.increment());
        } else {
            from("direct:test")
                    .threads(batchSize)
                    .process(exchange -> longAdder.increment());
        }
    }
}
