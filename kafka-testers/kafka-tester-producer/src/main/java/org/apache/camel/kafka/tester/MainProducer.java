package org.apache.camel.kafka.tester;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.HdrHistogram.EncodableHistogram;
import org.HdrHistogram.SingleWriterRecorder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.dataset.ListDataSet;
import org.apache.camel.component.dataset.SimpleDataSet;
import org.apache.camel.kafka.tester.common.TestMainListener;
import org.apache.camel.kafka.tester.io.common.FileHeader;
import org.apache.camel.kafka.tester.io.BinaryRateWriter;
import org.apache.camel.kafka.tester.io.LatencyWriter;
import org.apache.camel.kafka.tester.io.RateWriter;
import org.apache.camel.main.Main;

/**
 * A Camel Application
 */
public class MainProducer {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
        Main main = new Main();


        final String testRateFileName = System.getProperty("test.rate.file", "producer-rate.data");

        final LongAdder longAdder = new LongAdder();
        int testSize = Integer.parseInt(System.getProperty("camel.main.durationMaxMessages", "0"));

        bindDataSet(main, testSize);

        File testRateFile = new File(testRateFileName);
        try (RateWriter rateWriter = new BinaryRateWriter(testRateFile, FileHeader.WRITER_DEFAULT_PRODUCER)) {
            RouteBuilder routeBuilder = getRouteBuilder(longAdder);
            main.configure().addRoutesBuilder(routeBuilder);
            main.addMainListener(new TestMainListener(rateWriter, longAdder, testSize, main::stop));

            main.run();
        }
    }

    private static RouteBuilder getRouteBuilder(LongAdder longAdder) {
        String routeType = System.getProperty("test.producer.type", "kafka");

        switch (routeType) {
            case "kafka": return getKafkaRouteBuilder(longAdder);
            case "noop": return getTestNoopProducer(longAdder);
        }

        throw new IllegalArgumentException("Invalid route type: " + routeType);
    }

    private static TestNoopProducer getTestNoopProducer(LongAdder longAdder) {
        int batchSize = Integer.parseInt(System.getProperty("test.batch.size", "0"));
        if (batchSize > 0) {
            return new TestNoopProducer(longAdder, true, batchSize);
        } else {
            return new TestNoopProducer(longAdder, false, batchSize);
        }
    }

    private static KafkaProducerRouteBuilder getKafkaRouteBuilder(LongAdder longAdder) {
        final String topic = System.getProperty("kafka.topic", "test-topic-producer");
        int batchSize = Integer.parseInt(System.getProperty("test.batch.size", "0"));
        SingleWriterRecorder latencyRecorder = new SingleWriterRecorder(TimeUnit.HOURS.toMicros(1), 3);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> saveLatencyFile(latencyRecorder)));

        if (batchSize > 0) {
            return new KafkaProducerRouteBuilder(latencyRecorder, longAdder, true, batchSize, topic);
        } else {
            return new KafkaProducerRouteBuilder(latencyRecorder, longAdder, false, batchSize, topic);
        }
    }

    private static void saveLatencyFile(SingleWriterRecorder latencyRecorder) {
        final String testLatenciesFileName = System.getProperty("test.latencies.file", "producer-latencies.hdr");
        try (LatencyWriter latencyWriter = new LatencyWriter(new File(testLatenciesFileName))) {
            EncodableHistogram histogram = latencyRecorder.getIntervalHistogram();

            String camelVersion = System.getProperty("camel.version", "3.x.x");
            histogram.setTag(camelVersion);
            latencyWriter.outputIntervalHistogram(histogram);
        } catch (IOException e) {
            System.err.println("Unable to save latency file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void bindDataSet(Main main, int testSize) {
        SimpleDataSet simpleDataSet = new SimpleDataSet();

        simpleDataSet.setDefaultBody(Boolean.TRUE);
        simpleDataSet.setSize(testSize);

        main.bind("testSet", simpleDataSet);
    }
}

