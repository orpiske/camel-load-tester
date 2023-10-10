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

public class FilterXpathPositive extends ThreadedProducerTemplate {
    private static String JBOURNE_DATA = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><person user=\"jbourne\"><firstName>Jason</firstName><lastName>Bourne</lastName><city>London</city></person>";

    private static final Logger LOG = LoggerFactory.getLogger(FilterXpathPositive.class);
    private final LongAdder longAdder;
    private final int threadCountConsumer;

    public FilterXpathPositive() {
        super(Parameters.threadCountProducer());

        this.longAdder = Counter.getInstance().getAdder();
        this.threadCountConsumer = Parameters.threadCountConsumer();
    }

    protected void produceMessages(int numMessages, ProducerTemplate producerTemplate, Endpoint endpoint) {
        LOG.info("Sending {} messages from {}", numMessages, Thread.currentThread().getId());

        for (int i = 0; i < numMessages; i++) {
            producerTemplate.sendBody(endpoint, JBOURNE_DATA);
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
                    .process(this::noopProcess)
                    .end()
                .to("log:?level=OFF");
    }
}
