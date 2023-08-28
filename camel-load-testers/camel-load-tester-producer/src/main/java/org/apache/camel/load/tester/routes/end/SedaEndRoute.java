package org.apache.camel.load.tester.routes.end;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.load.tester.common.Counter;
import org.apache.camel.load.tester.common.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SedaEndRoute extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(SedaEndRoute.class);
    private final int threadCountConsumer;
    private final LongAdder longAdder;

    public SedaEndRoute() {
        this.threadCountConsumer = Parameters.threadCountConsumer();
        this.longAdder = Counter.getInstance().getAdder();
    }

    @Override
    public void configure() {
        LOG.info("Using thread count for parallel consumption: {}", threadCountConsumer);

        if (threadCountConsumer == 0) {
            from("seda:test")
                    .routeId("noop-to-seda")
                    .process(exchange -> longAdder.increment());
        } else {
            fromF("seda:test?concurrentConsumers=%s", threadCountConsumer)
                    .routeId("noop-to-seda-threaded")
                    .process(exchange -> longAdder.increment());
        }
    }
}
