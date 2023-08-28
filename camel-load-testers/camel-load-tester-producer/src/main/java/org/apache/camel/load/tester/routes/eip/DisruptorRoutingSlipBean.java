package org.apache.camel.load.tester.routes.eip;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.DynamicRouter;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.load.tester.common.Counter;
import org.apache.camel.load.tester.common.Parameters;
import org.apache.camel.load.tester.routes.ThreadedProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisruptorRoutingSlipBean extends ThreadedProducerTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(DisruptorRoutingSlipBean.class);
    private final LongAdder longAdder;

    public DisruptorRoutingSlipBean() {
        super(Parameters.threadCountProducer());

        this.longAdder = Counter.getInstance().getAdder();
    }

    protected void produceMessages(int numMessages, ProducerTemplate producerTemplate, Endpoint endpoint1, Endpoint endpoint2) {
        LOG.info("Sending {} messages from {}", numMessages, Thread.currentThread().getId());

        for (int i = 0; i < numMessages; i++) {
            producerTemplate.sendBody(endpoint1, "test-string");
            producerTemplate.sendBody(endpoint2, "test-string");
        }

    }

    @Override
    protected void produceMessagesWithRate(int numMessages) {
        final ProducerTemplate producerTemplate = getCamelContext().createProducerTemplate();
        final Endpoint endpoint = getCamelContext().getEndpoint("disruptor:dynamic-router-start");

        produceMessagesWithRate(numMessages, producerTemplate, endpoint);
    }


    @Override
    protected void produceMessages(int numMessages) {
        final ProducerTemplate producerTemplate = getCamelContext().createProducerTemplate();
        final Endpoint endpoint1 = getCamelContext().getEndpoint("direct:start-1");
        final Endpoint endpoint2 = getCamelContext().getEndpoint("direct:start-2");

        produceMessages(numMessages, producerTemplate, endpoint1, endpoint2);
    }

    @Override
    protected void produce(Exchange exchange) {
        if (super.getTargetRate() == 0) {
            for (int i = 0; i < super.getThreadCount(); i++) {
                super.getExecutorService().submit(() -> produceMessages(super.getTestSize() / super.getThreadCount()));
            }
        } else {
            throw new UnsupportedOperationException("Producing with target rate is not supported yet");
        }
    }

    private void noopProcess(Exchange exchange) {
        longAdder.increment();
    }

    private void noopProcess2(Exchange exchange) {
        longAdder.increment();
    }

    @Override
    public void configure() {
        LOG.info("Using thread count for parallel production: {}", getThreadCount());

        from("timer:start?repeatCount=1&delay=2000")
                .to("direct:start");

        from("direct:start")
                .process(this::produce);

        from("direct:start-1")
                .bean(new MyDynamicRouterPojo("disruptor:slip-route-1"));

        from("direct:start-2")
                .bean(new MyDynamicRouterPojo("disruptor:slip-route-2"));

        from("disruptor:slip-route-1")
                .process(this::noopProcess);

        from("disruptor:slip-route-2")
                .process(this::noopProcess2);
    }


    public static class MyDynamicRouterPojo {

        private final String target;

        public MyDynamicRouterPojo(String target) {
            this.target = target;
        }

        @DynamicRouter
        public String route(@Header(Exchange.SLIP_ENDPOINT) String previous) {
            if (previous == null) {
                return target;
            } else {
                return null;
            }
        }
    }
}
