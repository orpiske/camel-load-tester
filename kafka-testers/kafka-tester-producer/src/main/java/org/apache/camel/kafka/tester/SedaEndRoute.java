package org.apache.camel.kafka.tester;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.builder.RouteBuilder;

public class SedaEndRoute extends RouteBuilder {
    private final int threadCount;
    private final LongAdder longAdder;

    public SedaEndRoute(int threadCount, LongAdder longAdder) {
        this.threadCount = threadCount;
        this.longAdder = longAdder;
    }

    @Override
    public void configure() {
        if (threadCount == 0) {
            from("seda:test")
                    .routeId("noop-to-seda")
                    .process(exchange -> longAdder.increment());
        } else {
            fromF("seda:test?concurrentConsumers=%s", threadCount)
                    .routeId("noop-to-seda-threaded")
                    .process(exchange -> longAdder.increment());
        }
    }
}
