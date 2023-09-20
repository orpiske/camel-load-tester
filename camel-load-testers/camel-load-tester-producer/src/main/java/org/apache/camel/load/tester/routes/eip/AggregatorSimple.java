package org.apache.camel.load.tester.routes.eip;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.load.tester.common.Counter;
import org.apache.camel.load.tester.common.Parameters;
import org.apache.camel.load.tester.routes.ThreadedProducerTemplate;
import org.apache.camel.processor.aggregate.GroupedExchangeAggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AggregatorSimple extends ThreadedProducerTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(AggregatorSimple.class);
    private final LongAdder longAdder;
    private final int batchSize;

    public AggregatorSimple() {
        super(Parameters.threadCountProducer());

        this.longAdder = Counter.getInstance().getAdder();
        this.batchSize = Parameters.batchSize() == 0 ? 3 : Parameters.batchSize();
    }

    protected void produceMessages(int numMessages, ProducerTemplate producerTemplate, Endpoint endpoint) {
        LOG.info("Sending {} messages from {}", numMessages, Thread.currentThread().getId());

        for (int i = 0; i < numMessages; i++) {
            producerTemplate.sendBody(endpoint, "test-string");
        }
    }

    protected void produceMessagesWithRate(int numMessages) {
        final ProducerTemplate producerTemplate = getCamelContext().createProducerTemplate();
        final Endpoint endpoint = getCamelContext().getEndpoint("disruptor:aggregator-start");

        produceMessagesWithRate(numMessages, producerTemplate, endpoint);
    }


    protected void produceMessages(int numMessages) {
        final ProducerTemplate producerTemplate = getCamelContext().createProducerTemplate();
        final Endpoint endpoint = getCamelContext().getEndpoint("disruptor:aggregator-start");

        produceMessages(numMessages, producerTemplate, endpoint);
    }

    private void noopProcess(Exchange exchange) {
        longAdder.increment();
    }

    @Override
    public void configure() {
        LOG.info("Using thread count for parallel production: {}", getProducerThreadCount());

        from("timer:start?repeatCount=1&delay=2000").to("direct:start");

        from("disruptor:aggregator-start")
                .aggregate(constant(true), new GroupedExchangeAggregationStrategy())
                .completionSize(batchSize)
                .process(this::noopProcess);

        from("direct:start")
                .process(this::produce);
    }
}
