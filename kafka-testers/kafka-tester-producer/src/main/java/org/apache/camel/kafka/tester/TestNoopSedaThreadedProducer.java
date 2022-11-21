package org.apache.camel.kafka.tester;

import java.util.concurrent.atomic.LongAdder;

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
        from("dataset:testSet?produceDelay=0&minRate={{?min.rate}}&initialDelay={{initial.delay:2000}}&dataSetIndex=off")
                .to("seda:test");

        if (threadCount == 0) {
            from("seda:test")
                    .process(exchange -> longAdder.increment());
        } else {
            fromF("seda:test?concurrentConsumers=%s", threadCount)
                    .threads(threadCount)
                    .process(exchange -> longAdder.increment());
        }
    }
}
