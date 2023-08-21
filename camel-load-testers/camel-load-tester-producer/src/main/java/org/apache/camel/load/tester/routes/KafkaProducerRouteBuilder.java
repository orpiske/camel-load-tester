package org.apache.camel.load.tester.routes;

import java.time.Instant;
import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.load.tester.common.Counter;
import org.apache.camel.load.tester.common.Parameters;
import org.apache.camel.processor.aggregate.GroupedExchangeAggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Camel Java DSL Router
 */
public class KafkaProducerRouteBuilder extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaProducerRouteBuilder.class);

    private final LongAdder longAdder;
    private final boolean aggregate;
    private final int batchSize;
    private final String topic;

    public KafkaProducerRouteBuilder() {
        this.longAdder = Counter.getInstance().getAdder();
        this.batchSize = Parameters.batchSize();
        this.aggregate = batchSize > 0;
        this.topic = Parameters.kafkaTopic();
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        final Counter counter = Counter.getInstance();

        counter.setupLatencyRecorder();

        if (!aggregate) {
            from("dataset:testSet?produceDelay=0&minRate={{?min.rate}}&initialDelay={{initial.delay:2000}}&dataSetIndex=off")
                    .routeId("kafka-non-aggregate")
                    .setProperty("CREATE_TIME", Instant::now)
                    .toF("kafka:%s", topic)
                    .process(exchange -> longAdder.increment())
                    .process(counter::measureExchange);
        } else {
            LOG.info("Using batch size: {}", batchSize);

            from("dataset:testSet?produceDelay=0&initialDelay={{initial.delay:2000}}&minRate={{?min.rate}}&preloadSize={{?preload.size}}&dataSetIndex=off")
                    .routeId("kafka-aggregate")
                    .aggregate(constant(true), new GroupedExchangeAggregationStrategy())
                    .completionSize(batchSize)
                    .setProperty("CREATE_TIME", Instant::now)
                    .toF("kafka:%s", topic)
                    .process(exchange -> longAdder.add(batchSize))
                    .process(counter::measureExchange);
        }
    }


}
