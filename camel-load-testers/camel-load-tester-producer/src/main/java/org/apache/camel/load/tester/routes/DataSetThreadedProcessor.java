package org.apache.camel.load.tester.routes;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.load.tester.common.Counter;
import org.apache.camel.load.tester.common.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Camel Java DSL Router
 */
public class DataSetThreadedProcessor extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(DataSetThreadedProcessor.class);

    private final LongAdder longAdder;
    private final int threadCountProcessor;

    public DataSetThreadedProcessor() {
        this.longAdder = Counter.getInstance().getAdder();
        this.threadCountProcessor = Parameters.threadCountProcessor();
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        LOG.info("Using thread count for processors: {}", threadCountProcessor);

        if (threadCountProcessor == 0) {
            from("dataset:testSet?produceDelay=0&minRate={{?min.rate}}&initialDelay={{initial.delay:2000}}&dataSetIndex=off")
                    .routeId("dataset-single-threaded")
                    .process(exchange -> longAdder.increment());
        } else {
            from("dataset:testSet?produceDelay=0&minRate={{?min.rate}}&initialDelay={{initial.delay:2000}}&dataSetIndex=off")
                    .routeId("dataset-threaded")
                    .threads(threadCountProcessor)
                    .process(exchange -> longAdder.increment());
        }
    }
}
