package org.apache.camel.kafka.tester;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.builder.RouteBuilder;

/**
 * A Camel Java DSL Router
 */
public class MyConsumer extends RouteBuilder {
    private final LongAdder longAdder;

    public MyConsumer(LongAdder longAdder) {
        this.longAdder = longAdder;
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        from("kafka:test?autoOffsetReset=earliest")
            .process(exchange -> longAdder.increment());
    }

}
