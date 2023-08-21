package org.apache.camel.load.tester.routes;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.load.tester.routes.eip.DisruptorCBR;
import org.apache.camel.load.tester.routes.eip.DisruptorRoutingSlipBean;
import org.apache.camel.load.tester.common.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Routes {
    private static final Logger LOG = LoggerFactory.getLogger(Routes.class);
    private static final Map<String, Supplier<RouteBuilder>> routes;
    private static final Map<String, Supplier<RouteBuilder>> endRoutes;

    static {
        routes = createRouteBuildersMapping();
        endRoutes = createEndRouteBuilders();
    }

    private Routes() {
    }

    private static Map<String, Supplier<RouteBuilder>> createRouteBuildersMapping() {
        Map<String, Supplier<RouteBuilder>> routes = new HashMap<>();

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
        routes.put("threaded-controlbus-producer", ControlBusThreadedProducerTemplate::new);
        routes.put("eip-cbr-text-route", DisruptorCBR::new);
        routes.put("eip-routing-slip-bean-disruptor", DisruptorRoutingSlipBean::new);

        return routes;
    }

    private static Map<String, Supplier<RouteBuilder>> createEndRouteBuilders() {
        Map<String, Supplier<RouteBuilder>> routes = new HashMap<>();

        routes.put("dataset-batched-processor", null);
        routes.put("dataset-injection-to-direct", DirectEndRoute::new);
        routes.put("dataset-injection-to-seda", SedaEndRoute::new);
        routes.put("dataset-noop-to-direct", DirectEndRoute::new);
        routes.put("dataset-noop-to-seda", SedaEndRoute::new);
        routes.put("dataset-threaded-processor", null);
        routes.put("kafka", null);
        routes.put("threaded-producer", SedaEndRoute::new);
        routes.put("threaded-seda-producer", SedaEndRoute::new);
        routes.put("threaded-disruptor-producer", DisruptorEndRoute::new);
        routes.put("threaded-controlbus-producer", null);
        routes.put("eip-cbr-text-route", null);
        routes.put("eip-routing-slip-bean-disruptor", null);

        return routes;
    }

    public static RouteBuilder getRouteBuilder() {
        String routeType = System.getProperty(Parameters.TEST_PRODUCER_TYPE, "kafka");
        LOG.info("Creating a new producer of type {}", routeType);

        Supplier<RouteBuilder> supplier = routes.get(routeType);

        if (supplier == null) {
            throw new IllegalArgumentException("Invalid route type: " + routeType);
        }

        return supplier.get();
    }

    public static RouteBuilder getEndRouteBuilder() {
        String routeType = System.getProperty(Parameters.TEST_PRODUCER_TYPE, "kafka");
        LOG.info("Creating a new producer of type {}", routeType);

        Supplier<RouteBuilder> supplier = endRoutes.get(routeType);
        if (supplier != null) {
            return supplier.get();
        }

        return null;
    }
}
