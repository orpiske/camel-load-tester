package org.apache.camel.kafka.tester;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Camel Java DSL Router
 */
public class TestNoopSedaThreadedProducer extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(TestNoopSedaThreadedProducer.class);

    private final LongAdder longAdder;
    private final int threadCount;

    public TestNoopSedaThreadedProducer(LongAdder longAdder, int threadCount) {
        this.longAdder = longAdder;
        this.threadCount = threadCount;
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        onException(IllegalStateException.class)
                .process(e -> LOG.error("The SEDA queue is likely full and the system may be unable to catch to the load. Fix the test parameters"));

        LOG.info("Using thread count: {}", threadCount);

        from("dataset:testSet?produceDelay=0&minRate={{?min.rate}}&initialDelay={{initial.delay:2000}}&dataSetIndex=off")
                .routeId("noop-to-seda")
                .to("seda:test?blockWhenFull=true&offerTimeout=1000");
    }
}
