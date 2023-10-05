package org.apache.camel.load.tester.routes.eip;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.load.tester.routes.ThreadedProducerTemplate;
import org.apache.camel.load.tester.common.Counter;
import org.apache.camel.load.tester.common.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisruptorCBR extends ThreadedProducerTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(DisruptorCBR.class);
    private final LongAdder longAdder;
    private final boolean heterogeneousPayload;

    public DisruptorCBR() {
        super(Parameters.threadCountProducer());

        this.longAdder = Counter.getInstance().getAdder();
        heterogeneousPayload = Boolean.parseBoolean(System.getProperty("eip.cbr.heterogeneousPayload", "true"));

    }

    protected void produceMessages(int numMessages, ProducerTemplate producerTemplate, Endpoint endpoint) {
        if (heterogeneousPayload) {
            super.produceMessages(numMessages, producerTemplate, endpoint);
        } else {
            LOG.info("Sending {} messages from {}", numMessages, Thread.currentThread().getId());

            for (int i = 0; i < numMessages; i++) {
                producerTemplate.sendBody(endpoint, "test-string");
            }
        }
    }

    protected void produceMessagesWithRate(int numMessages) {
        final ProducerTemplate producerTemplate = getCamelContext().createProducerTemplate();
        final Endpoint endpoint = getCamelContext().getEndpoint("disruptor:cbr-start");

        produceMessagesWithRate(numMessages, producerTemplate, endpoint);
    }


    protected void produceMessages(int numMessages) {
        final ProducerTemplate producerTemplate = getCamelContext().createProducerTemplate();
        final Endpoint endpoint = getCamelContext().getEndpoint("disruptor:cbr-start");

        produceMessages(numMessages, producerTemplate, endpoint);
    }

    private void noopProcess(Exchange exchange) {
        longAdder.increment();
    }

    private void noopProcess2(Exchange exchange) {
        longAdder.increment();
    }

    @Override
    public void configure() {
        LOG.info("Using thread count for parallel production: {}", getProducerThreadCount());

        from("timer:start?repeatCount=1&delay=2000").to("direct:start");

        from("disruptor:cbr-start")
                .choice()
                .when(body().contains("test-string"))
                    .process(this::noopProcess)
                .otherwise()
                    .process(this::noopProcess2)
                .end();


        from("direct:start")
                .process(this::produce);
    }
}
