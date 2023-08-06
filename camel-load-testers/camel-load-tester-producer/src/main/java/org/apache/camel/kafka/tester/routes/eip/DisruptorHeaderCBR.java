package org.apache.camel.kafka.tester.routes.eip;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.kafka.tester.common.Counter;
import org.apache.camel.kafka.tester.common.Parameters;
import org.apache.camel.kafka.tester.routes.ThreadedProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisruptorHeaderCBR extends ThreadedProducerTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(DisruptorHeaderCBR.class);
    private final LongAdder longAdder;
    private final boolean heterogeneousPayload;

    public DisruptorHeaderCBR() {
        super(Parameters.threadCountProducer());

        this.longAdder = Counter.getInstance().getAdder();
        heterogeneousPayload = Boolean.valueOf(System.getProperty("eip.cbr.heterogeneousPayload", "true"));
    }

    protected void produceHeterogeneousMessagesWithHeader(int numMessages, ProducerTemplate producerTemplate, Endpoint endpoint) {
        LOG.info("Sending {} messages from {}", numMessages, Thread.currentThread().getId());
        Object[] data = new Object[]{"test-string", getSomeFile(), getSomeInt(), getSampleObject()};
        Object[] dataPayload = new Object[]{"type1", "type2"};

        for (int i = 0; i < numMessages; i++) {
            Object payload = data[i % data.length];
            producerTemplate.sendBodyAndHeader(endpoint, payload, "payload", dataPayload[i % dataPayload.length]);
        }
    }

    @Override
    protected void produceMessages(int numMessages, ProducerTemplate producerTemplate, Endpoint endpoint) {
        if (heterogeneousPayload) {
            produceHeterogeneousMessagesWithHeader(numMessages, producerTemplate, endpoint);
        } else {
            LOG.info("Sending {} messages from {}", numMessages, Thread.currentThread().getId());
            Object[] dataPayload = new Object[]{"type1", "type2"};

            for (int i = 0; i < numMessages; i++) {
                producerTemplate.sendBodyAndHeader(endpoint, "test-string", "payload", dataPayload[i % dataPayload.length]);
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
        LOG.info("Using thread count for parallel production: {}", getThreadCount());

        from("timer:start?repeatCount=1&delay=2000").to("direct:start");

        from("disruptor:cbr-start")
                .choice()
                .when(simple("${header.payload} == 'type1'"))
                    .process(this::noopProcess)
                .otherwise()
                    .process(this::noopProcess2)
                .end();


        from("direct:start")
                .process(this::produce);
    }
}
