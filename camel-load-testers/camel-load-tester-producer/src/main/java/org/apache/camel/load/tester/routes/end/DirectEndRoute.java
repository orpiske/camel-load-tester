package org.apache.camel.load.tester.routes.end;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.load.tester.common.Counter;
import org.apache.camel.load.tester.common.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectEndRoute extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(DirectEndRoute.class);

    private final int threadCountProcessors;
    private final LongAdder longAdder;

    public DirectEndRoute() {
        this.threadCountProcessors = Parameters.threadCountProcessor();
        this.longAdder = Counter.getInstance().getAdder();
    }

    @Override
    public void configure() {
        LOG.info("Using thread count for parallel processing: {}", threadCountProcessors);

        if (threadCountProcessors == 0) {
            from("direct:test")
                    .routeId("noop-to-direct")
                    .process(exchange -> longAdder.increment());
        } else {
            from("direct:test")
                    .routeId("noop-to-direct-threaded")
                    .threads(threadCountProcessors)
                    .process(exchange -> longAdder.increment());
        }
    }
}
