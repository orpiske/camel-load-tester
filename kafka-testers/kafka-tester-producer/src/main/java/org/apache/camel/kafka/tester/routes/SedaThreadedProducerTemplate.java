package org.apache.camel.kafka.tester.routes;

import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.kafka.tester.common.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SedaThreadedProducerTemplate extends ThreadedProducerTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(SedaThreadedProducerTemplate.class);

    public SedaThreadedProducerTemplate() {
        super(Parameters.threadCountProducer());
    }

    protected void produceMessagesWithRate(int numMessages) {
        final ProducerTemplate producerTemplate = getCamelContext().createProducerTemplate();
        final Endpoint endpoint = getCamelContext().getEndpoint("seda:test?blockWhenFull=true&offerTimeout=1000");

        produceMessagesWithRate(numMessages, producerTemplate, endpoint);
    }


    protected void produceMessages(int numMessages) {
        final ProducerTemplate producerTemplate = getCamelContext().createProducerTemplate();
        final Endpoint endpoint = getCamelContext().getEndpoint("seda:test?blockWhenFull=true&offerTimeout=1000");

        produceMessages(numMessages, producerTemplate, endpoint);
    }

    @Override
    public void configure() {
        LOG.info("Using thread count for parallel production: {}", getThreadCount());

        onException(IllegalStateException.class)
                .process(e -> LOG.error("The SEDA queue is likely full and the system may be unable to catch to the load. Fix the test parameters"));

        from("timer:start?repeatCount=1&delay=2000").to("direct:start");

        from("direct:start")
                .process(this::produce);
    }
}
