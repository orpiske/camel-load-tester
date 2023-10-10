package org.apache.camel.load.tester.routes.eip;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.load.tester.common.Counter;
import org.apache.camel.load.tester.common.Parameters;
import org.apache.camel.load.tester.routes.ThreadedProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterXpathNegative extends ThreadedProducerTemplate {
    private static String JSPARROW_DATA = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><person user=\"jsparrow\"><firstName>Jack</firstName><lastName>Sparrow</lastName><city>Port Royal</city></person>";

    private static final Logger LOG = LoggerFactory.getLogger(FilterXpathNegative.class);
    private final LongAdder longAdder;
    private final int threadCountConsumer;

    public FilterXpathNegative() {
        super(Parameters.threadCountProducer());

        this.longAdder = Counter.getInstance().getAdder();
        this.threadCountConsumer = Parameters.threadCountConsumer();
    }

    @Override
    protected void produceMessages(int numMessages, ProducerTemplate producerTemplate, Endpoint endpoint) {
        LOG.info("Sending {} messages from {}", numMessages, Thread.currentThread().getId());

        for (int i = 0; i < numMessages; i++) {
            producerTemplate.sendBody(endpoint, JSPARROW_DATA);
        }
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

    private void noopProcess(Exchange exchange) {
        longAdder.increment();
    }

    @Override
    public void configure() {
        from("timer:start?repeatCount=1&delay=2000").to("direct:start");

        from("direct:start")
                .process(this::produce);

        // Xpath evaluation is slow, so let's parallelize it for greater speed
        from("disruptor:test")
                .threads(threadCountConsumer, threadCountConsumer)
                .filter().xpath("/person[@user='jbourne']")
                    .to("log:?level=OFF")
                    .end()
                .process(this::noopProcess);
    }
}
