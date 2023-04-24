package org.apache.camel.kafka.tester.routes;

import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.kafka.tester.common.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisruptorVMThreadedProducerTemplate extends ThreadedProducerTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(DisruptorVMThreadedProducerTemplate.class);

    public DisruptorVMThreadedProducerTemplate() {
        super(Parameters.threadCountProducer());
    }

    protected void produceMessagesWithRate(int numMessages) {
        final ProducerTemplate producerTemplate = getCamelContext().createProducerTemplate();
        final Endpoint endpoint = getCamelContext().getEndpoint("disruptor:test");

        produceMessagesWithRate(numMessages, producerTemplate, endpoint);
    }


    protected void produceMessages(int numMessages) {
        final ProducerTemplate producerTemplate = getCamelContext().createProducerTemplate();
        final Endpoint endpoint = getCamelContext().getEndpoint("disruptor:test");

        produceMessages(numMessages, producerTemplate, endpoint);
    }

    @Override
    public void configure() {
        LOG.info("Using thread count for parallel production: {}", getThreadCount());

        from("timer:start?repeatCount=1&delay=2000").to("direct:start");

        from("direct:start")
                .process(this::produce);
    }
}
