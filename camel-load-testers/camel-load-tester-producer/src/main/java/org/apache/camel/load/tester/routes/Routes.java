package org.apache.camel.load.tester.routes;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.load.tester.routes.eip.AggregatorSimple;
import org.apache.camel.load.tester.routes.eip.DisruptorCBR;
import org.apache.camel.load.tester.routes.eip.DisruptorRoutingSlipBean;
import org.apache.camel.load.tester.common.Parameters;
import org.apache.camel.load.tester.routes.eip.FilterDataSonnetPositive;
import org.apache.camel.load.tester.routes.eip.FilterTextNegative;
import org.apache.camel.load.tester.routes.eip.FilterTextPositive;
import org.apache.camel.load.tester.routes.eip.FilterXpathNegative;
import org.apache.camel.load.tester.routes.eip.FilterXpathPositive;
import org.apache.camel.load.tester.routes.eip.ToDHeaderTest;
import org.apache.camel.load.tester.routes.end.DirectEndRoute;
import org.apache.camel.load.tester.routes.end.DisruptorEndRoute;
import org.apache.camel.load.tester.routes.end.SedaEndRoute;
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
        routes.put("dataset-noop-header-tod", ToDHeaderTest::new);
        routes.put("kafka", KafkaProducerRouteBuilder::new);
        routes.put("threaded-producer", SedaThreadedProducerTemplate::new);
        routes.put("threaded-seda-producer", SedaThreadedProducerTemplate::new);
        routes.put("threaded-disruptor-producer", DisruptorVMThreadedProducerTemplate::new);
        routes.put("threaded-controlbus-producer", ControlBusThreadedProducerTemplate::new);
        routes.put("eip-cbr-text-route", DisruptorCBR::new);
        routes.put("eip-routing-slip-bean-disruptor", DisruptorRoutingSlipBean::new);
        routes.put("eip-aggregator-simple-route", AggregatorSimple::new);
        routes.put("filter-xpath-positive", FilterXpathPositive::new);
        routes.put("filter-xpath-negative", FilterXpathNegative::new);
        routes.put("filter-text-positive", FilterTextPositive::new);
        routes.put("filter-text-negative", FilterTextNegative::new);
        routes.put("filter-datasonnet-positive", FilterDataSonnetPositive::new);

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
        routes.put("dataset-noop-header-tod", null);
        routes.put("kafka", null);
        routes.put("threaded-producer", SedaEndRoute::new);
        routes.put("threaded-seda-producer", SedaEndRoute::new);
        routes.put("threaded-disruptor-producer", DisruptorEndRoute::new);
        routes.put("threaded-controlbus-producer", null);
        routes.put("eip-cbr-text-route", null);
        routes.put("eip-routing-slip-bean-disruptor", null);
        routes.put("eip-aggregator-simple-route", null);
        routes.put("filter-xpath-positive", null);
        routes.put("filter-xpath-negative", null);
        routes.put("filter-text-positive", null);
        routes.put("filter-text-negative", null);
        routes.put("filter-datasonnect-positive", null);

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


        Supplier<RouteBuilder> supplier = endRoutes.get(routeType);
        if (supplier != null) {
            LOG.info("Creating a new end route of type {}", routeType);
            return supplier.get();
        }

        LOG.info("No end route available for route type {}", routeType);
        return null;
    }
}
