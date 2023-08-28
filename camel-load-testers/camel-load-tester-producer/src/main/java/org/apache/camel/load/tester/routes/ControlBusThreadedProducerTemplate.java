package org.apache.camel.load.tester.routes;

import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.load.tester.common.Counter;
import org.apache.camel.load.tester.common.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlBusThreadedProducerTemplate extends ThreadedProducerTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(ControlBusThreadedProducerTemplate.class);
    private final LongAdder longAdder;

    public ControlBusThreadedProducerTemplate() {
        super(Parameters.threadCountProducer());

        this.longAdder = Counter.getInstance().getAdder();
    }

    protected void produceMessagesWithRate(int numMessages) {
        final ProducerTemplate producerTemplate = getCamelContext().createProducerTemplate();
        final Endpoint endpoint = getCamelContext().getEndpoint("controlbus:route?routeId=route1&action=status&loggingLevel=off");

        produceMessagesWithRate(numMessages, producerTemplate, endpoint);
    }

    protected void produceMessages(int numMessages, ProducerTemplate producerTemplate, String endpoint) {
        LOG.info("Sending {} messages from {}", numMessages, Thread.currentThread().getId());
        List<Object> data = List.of("test-string", getSomeFile(), getSomeInt(), getSampleObject());

        for (int i = 0; i < numMessages; i++) {
            Object payload = data.get(i % data.size());
            producerTemplate.sendBody(endpoint, payload);

            longAdder.increment();
        }
    }

    // Note: in this test we want to force Camel to resolve the endpoint every time
    protected void produceMessages(int numMessages) {
        final ProducerTemplate producerTemplate = getCamelContext().createProducerTemplate();
        final String endpoint = "controlbus:route?routeId=route1&action=status&loggingLevel=off";

        produceMessages(numMessages, producerTemplate, endpoint);
    }

    @Override
    public void configure() {
        LOG.info("Using thread count for parallel production: {}", getProducerThreadCount());

        from("timer:start?repeatCount=1&delay=2000").to("direct:start");

        from("direct:start")
                .process(this::produce);
    }
}
