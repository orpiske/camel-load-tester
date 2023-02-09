package org.apache.camel.kafka.tester;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.builder.RouteBuilder;

public class DirectEndRoute extends RouteBuilder {
    private final int threadCount;
    private final LongAdder longAdder;

    public DirectEndRoute(int threadCount, LongAdder longAdder) {
        this.threadCount = threadCount;
        this.longAdder = longAdder;
    }

    @Override
    public void configure() {
        if (threadCount == 0) {
            from("direct:test")
                    .routeId("noop-to-direct")
                    .process(exchange -> longAdder.increment());
        } else {
            from("direct:test")
                    .routeId("noop-to-direct-threaded")
                    .threads(threadCount)
                    .process(exchange -> longAdder.increment());
        }
    }
}
