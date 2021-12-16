package org.apache.camel.kafka.tester;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.HdrHistogram.SingleWriterRecorder;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.GroupedExchangeAggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Camel Java DSL Router
 */
public class TestProducer extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(TestProducer.class);

    private final SingleWriterRecorder latencyRecorder;
    private final LongAdder longAdder;
    private final boolean aggregate;
    private final int batchSize;

    public TestProducer(SingleWriterRecorder latencyRecorder, LongAdder longAdder, boolean aggregate, int batchSize) {
        this.latencyRecorder = latencyRecorder;
        this.longAdder = longAdder;
        this.aggregate = aggregate;
        this.batchSize = batchSize;
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        if (!aggregate) {
            from("dataset:testSet?produceDelay=0&minRate={{?min.rate}}&initialDelay={{initial.delay:2000}}")
                    .setProperty("CREATE_TIME", Instant::now)
                    .to("kafka:test")
                    .process(exchange -> longAdder.increment())
                    .process(this::measureExchange);
        } else {
            from("dataset:testSet?produceDelay=0&initialDelay={{initial.delay:2000}}&minRate={{?min.rate}}&preloadSize={{?preload.size}}")
                    .aggregate(constant(true), new GroupedExchangeAggregationStrategy())
                    .completionSize(batchSize)
                    .setProperty("CREATE_TIME", Instant::now)
                    .to("kafka:test")
                    .process(exchange -> longAdder.add(batchSize))
                    .process(this::measureExchange);
        }
    }

    private void measureExchange(Exchange exchange) {
        Instant sent = exchange.getProperty("CREATE_TIME", Instant.class);
        if (sent != null) {
            Duration duration = Duration.between(sent, Instant.now());
            latencyRecorder.recordValue(TimeUnit.NANOSECONDS.toMicros(duration.toNanos()));
        } else {
            LOG.warn("Skipping latency processing for exchange due to missing CREATE_TIME property");
        }

    }

}
