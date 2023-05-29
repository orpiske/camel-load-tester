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

public class DisruptorCBR extends ThreadedProducerTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(DisruptorCBR.class);
    private final LongAdder longAdder;

    public DisruptorCBR() {
        super(Parameters.threadCountProducer());

        this.longAdder = Counter.getInstance().getAdder();
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
                .when(body().contains("test-string"))
                    .process(this::noopProcess)
                .otherwise()
                    .process(this::noopProcess2)
                .end();


        from("direct:start")
                .process(this::produce);
    }
}
