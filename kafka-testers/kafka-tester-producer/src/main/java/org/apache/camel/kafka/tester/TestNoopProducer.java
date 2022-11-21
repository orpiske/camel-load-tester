package org.apache.camel.kafka.tester;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.GroupedExchangeAggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Camel Java DSL Router
 */
public class TestNoopProducer extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(TestNoopProducer.class);

    private final LongAdder longAdder;
    private final boolean aggregate;
    private final int batchSize;

    public TestNoopProducer(LongAdder longAdder, boolean aggregate, int batchSize) {
        this.longAdder = longAdder;
        this.aggregate = aggregate;
        this.batchSize = batchSize;
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        LOG.info("Using batch size: {}", batchSize);

        if (!aggregate) {
            from("dataset:testSet?produceDelay=0&minRate={{?min.rate}}&initialDelay={{initial.delay:2000}}&dataSetIndex=off")
                    .process(exchange -> longAdder.increment());
        } else {
            from("dataset:testSet?produceDelay=0&initialDelay={{initial.delay:2000}}&minRate={{?min.rate}}&preloadSize={{?preload.size}}&dataSetIndex=off")
                    .aggregate(constant(true), new GroupedExchangeAggregationStrategy())
                    .completionSize(batchSize)
                    .process(exchange -> longAdder.add(batchSize));
        }
    }
}
