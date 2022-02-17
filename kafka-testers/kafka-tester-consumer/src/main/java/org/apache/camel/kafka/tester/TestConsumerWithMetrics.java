package org.apache.camel.kafka.tester;

import java.util.concurrent.atomic.LongAdder;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.influx.InfluxConfig;
import io.micrometer.influx.InfluxMeterRegistry;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.micrometer.MicrometerConstants;

/**
 * A Camel Java DSL Router
 */
public class TestConsumerWithMetrics extends RouteBuilder {
    private final LongAdder longAdder;

    public TestConsumerWithMetrics(LongAdder longAdder) {
        this.longAdder = longAdder;
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        MeterRegistry registry = getMetricRegistry();

        getContext().getRegistry().bind(MicrometerConstants.METRICS_REGISTRY_NAME, registry);

        from("kafka:test?autoOffsetReset=earliest")
            .process(exchange -> longAdder.increment())
            .to("micrometer:gauge:messages.count");
    }

    public MeterRegistry getMetricRegistry() {
        InfluxConfig config = new InfluxConfig() {

            @Override
            public String org() {
                return System.getProperty("metrics.org");
            }

            @Override
            public String bucket() {
                return System.getProperty("metrics.bucket", "kafka-tester-consumer");
            }

            @Override
            public String token() {
                return System.getProperty("metrics.token");
            }

            @Override
            public String uri() {
                return System.getProperty("metrics.uri");
            }

            @Override
            public String get(String k) {
                return null; // accept the rest of the defaults
            }
        };
        MeterRegistry registry = new InfluxMeterRegistry(config, Clock.SYSTEM);

        return registry;
    }

}
