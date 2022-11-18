package org.apache.camel.kafka.tester;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.GroupedExchangeAggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Camel Java DSL Router
 */
public class TestNoopThreadedProducer extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(TestNoopThreadedProducer.class);

    private final LongAdder longAdder;
    private final int threadCount;

    public TestNoopThreadedProducer(LongAdder longAdder, int batchSize) {
        this.longAdder = longAdder;
        this.threadCount = batchSize;
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        if (threadCount == 0) {
            from("dataset:testSet?produceDelay=0&minRate={{?min.rate}}&initialDelay={{initial.delay:2000}}&dataSetIndex=off")
                    .process(exchange -> longAdder.increment());
        } else {
            from("dataset:testSet?produceDelay=0&minRate={{?min.rate}}&initialDelay={{initial.delay:2000}}&dataSetIndex=off")
                    .threads(threadCount)
                    .process(exchange -> longAdder.increment());
        }
    }
}
