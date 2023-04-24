package org.apache.camel.kafka.tester.routes;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.kafka.tester.common.Parameters;

public final class Routes {
    private static final Map<String, Supplier<RouteBuilder>> routes;

    static {
        routes = new HashMap<>();

        routes.put("dataset-batched-processor", DataSetBatchedProcessor::new);
        routes.put("dataset-injection-to-direct", DataSetInjectionToDirect::new);
        routes.put("dataset-injection-to-seda", DataSetInjectionToSeda::new);
        routes.put("dataset-noop-to-direct", DataSetNoopToDirect::new);
        routes.put("dataset-noop-to-seda", DataSetNoopToSeda::new);
        routes.put("dataset-threaded-processor", DataSetThreadedProcessor::new);
        routes.put("kafka", KafkaProducerRouteBuilder::new);
        routes.put("threaded-producer", SedaThreadedProducerTemplate::new);
        routes.put("threaded-seda-producer", SedaThreadedProducerTemplate::new);
        routes.put("threaded-disruptor-producer", DisruptorVMThreadedProducerTemplate::new);
    }

    private Routes() {
    }

    public static RouteBuilder getRouteBuilder() {
        String routeType = System.getProperty(Parameters.TEST_PRODUCER_TYPE, "kafka");

        Supplier<RouteBuilder> supplier = routes.get(routeType);

        if (supplier == null) {
            throw new IllegalArgumentException("Invalid route type: " + routeType);
        }

        return supplier.get();
    }
}
