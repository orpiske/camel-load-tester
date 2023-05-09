package org.apache.camel.kafka.tester.routes;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.kafka.tester.common.Counter;
import org.apache.camel.kafka.tester.common.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectEndRoute extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(DirectEndRoute.class);

    private final int threadCount;
    private final LongAdder longAdder;

    public DirectEndRoute() {
        this.threadCount = Parameters.threadCount();
        this.longAdder = Counter.getInstance().getAdder();
    }

    @Override
    public void configure() {
        LOG.info("Using thread count for parallel processing: {}", threadCount);

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
