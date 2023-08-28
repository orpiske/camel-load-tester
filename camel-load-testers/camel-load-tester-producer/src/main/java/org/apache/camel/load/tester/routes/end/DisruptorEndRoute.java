package org.apache.camel.load.tester.routes.end;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.disruptor.DisruptorEndpoint;
import org.apache.camel.load.tester.common.Counter;
import org.apache.camel.load.tester.common.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisruptorEndRoute extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(DisruptorEndRoute.class);
    private final int threadCountConsumer;
    private final LongAdder longAdder;

    public DisruptorEndRoute() {
        this.threadCountConsumer = Parameters.threadCountConsumer();
        this.longAdder = Counter.getInstance().getAdder();
    }

    @Override
    public void configure() {
        LOG.info("Using thread count for parallel consumption: {}", threadCountConsumer);

        DisruptorEndpoint disruptor = getCamelContext().getEndpoint("disruptor:test", DisruptorEndpoint.class);
        int size = disruptor.getBufferSize();
        LOG.info("Using ring buffer size: {}", size);


        if (threadCountConsumer == 0) {
            from("disruptor:test")
                    .routeId("noop-to-disruptor")
                    .process(exchange -> longAdder.increment());
        } else {
            fromF("disruptor:test?concurrentConsumers=%s", threadCountConsumer)
                    .routeId("noop-to-disruptor-threaded")
                    .process(exchange -> longAdder.increment());
        }
    }
}
