package org.apache.camel.kafka.tester.routes;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.kafka.tester.common.Counter;

public class SedaEndRoute extends RouteBuilder {
    private final int threadCount;
    private final LongAdder longAdder;

    public SedaEndRoute(int threadCount) {
        this.threadCount = threadCount;
        this.longAdder = Counter.getInstance().getAdder();
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
