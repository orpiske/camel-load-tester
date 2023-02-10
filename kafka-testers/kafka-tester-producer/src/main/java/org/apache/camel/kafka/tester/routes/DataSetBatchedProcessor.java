package org.apache.camel.kafka.tester.routes;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.kafka.tester.common.Counter;
import org.apache.camel.kafka.tester.common.Parameters;
import org.apache.camel.processor.aggregate.GroupedExchangeAggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Camel Java DSL Router
 */
public class DataSetBatchedProcessor extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(DataSetBatchedProcessor.class);

    private final LongAdder longAdder;
    private final int batchSize;

    public DataSetBatchedProcessor() {
        this.longAdder = Counter.getInstance().getAdder();
        this.batchSize = Parameters.batchSize();
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        LOG.info("Using batch size: {}", batchSize);

        from("dataset:testSet?produceDelay=0&initialDelay={{initial.delay:2000}}&minRate={{?min.rate}}&preloadSize={{?preload.size}}&dataSetIndex=off")
            .routeId("dataset-batched-processor")
            .aggregate(constant(true), new GroupedExchangeAggregationStrategy())
            .completionSize(batchSize)
            .process(exchange -> longAdder.add(batchSize));

    }
}
