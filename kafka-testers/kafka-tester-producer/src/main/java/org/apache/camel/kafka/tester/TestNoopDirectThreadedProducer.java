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
    private final int threadCount;

    public TestNoopDirectThreadedProducer(LongAdder longAdder, int threadCount) {
        this.longAdder = longAdder;
        this.threadCount = threadCount;
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        LOG.info("Using thread count: {}", threadCount);

        from("dataset:testSet?produceDelay=0&minRate={{?min.rate}}&initialDelay={{initial.delay:2000}}&dataSetIndex=off")
                .to("direct:test");

        if (threadCount == 0) {
            from("direct:test")
                    .routeId("noop-to-direct")
                    .process(exchange -> longAdder.increment());
        } else {
            from("direct:test")
                    .routeId("noop-to-direct-threaded")
                    .threads(threadCount)
                    .process(exchange -> longAdder.increment());
        }
    }
}
