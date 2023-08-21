package org.apache.camel.load.tester;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.builder.RouteBuilder;

/**
 * A Camel Java DSL Router
 */
public class TestConsumer extends RouteBuilder {
    private final LongAdder longAdder;
    private final String topic;

    public TestConsumer(LongAdder longAdder, String topic) {
        this.longAdder = longAdder;
        this.topic = topic;
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        fromF("kafka:%s?autoOffsetReset=earliest", topic)
                .routeId("kafka-noop")
                .process(exchange -> longAdder.increment());
    }

}
