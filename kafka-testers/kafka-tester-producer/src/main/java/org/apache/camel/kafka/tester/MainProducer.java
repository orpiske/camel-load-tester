package org.apache.camel.kafka.tester;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.HdrHistogram.EncodableHistogram;
import org.HdrHistogram.SingleWriterRecorder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.dataset.SimpleDataSet;
import org.apache.camel.kafka.tester.common.IOUtil;
import org.apache.camel.kafka.tester.common.TestMainListener;
import org.apache.camel.kafka.tester.common.WriterReporter;
import org.apache.camel.kafka.tester.io.BinaryRateWriter;
import org.apache.camel.kafka.tester.io.LatencyWriter;
import org.apache.camel.kafka.tester.io.RateWriter;
import org.apache.camel.kafka.tester.io.common.FileHeader;
import org.apache.camel.main.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.kafka.tester.common.Parameters.threadCount;

/**
 * A Camel Application
 */
public class MainProducer {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) {
        Main main = new Main();
        final String testRateFileName = System.getProperty("test.rate.file", "producer-rate.data");

        final LongAdder longAdder = new LongAdder();
        int testSize = Integer.parseInt(System.getProperty("camel.main.durationMaxMessages", "0"));
        if (testSize == 0) {
            testSize = Integer.MAX_VALUE - 1;
        }

        bindDataSet(main, testSize);

        File testRateFile = IOUtil.create(testRateFileName);

        try (RateWriter rateWriter = new BinaryRateWriter(testRateFile, FileHeader.WRITER_DEFAULT_PRODUCER)) {
            int threadCount = threadCount();
            main.configure().addRoutesBuilder(new SedaEndRoute(threadCount, longAdder));
            main.configure().addRoutesBuilder(new DirectEndRoute(threadCount, longAdder));

            RouteBuilder routeBuilder = getRouteBuilder(longAdder);
            main.configure().addRoutesBuilder(routeBuilder);

            WriterReporter writerReporter = new WriterReporter(rateWriter, longAdder, testSize, main::shutdown);
            main.addMainListener(new TestMainListener(writerReporter));

            main.run();
        } catch (Exception e) {
            Logger log = LoggerFactory.getLogger(MainProducer.class);
            log.error("Unable to launch the test application: {}", e.getMessage(), e);
            main.shutdown();

            System.exit(1);
        }
    }

    private static RouteBuilder getRouteBuilder(LongAdder longAdder) {
        String routeType = System.getProperty("test.producer.type", "kafka");

        switch (routeType) {
            case "kafka": return getKafkaRouteBuilder(longAdder);
            case "dataset-batched-processor":
            case "noop": return getDataSetBatchedProcessor(longAdder);
            case "noop-threaded": return getDataSetThreadedProcessor(longAdder);
            case "dataset-noop-to-direct":
            case "noop-threaded-producer": return getDataSetNoopToDirect();
            case "dataset-noop-to-seda":
            case "noop-threaded-seda": return getDataSetNoopToSeda();
            case "threaded-producer": return getThreadedProducerTemplate();
        }

        throw new IllegalArgumentException("Invalid route type: " + routeType);
    }

    private static DataSetBatchedProcessor getDataSetBatchedProcessor(LongAdder longAdder) {
        int batchSize = Integer.parseInt(System.getProperty("test.batch.size", "1"));
        return new DataSetBatchedProcessor(longAdder, batchSize);
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
        final File path = IOUtil.create(testLatenciesFileName);

        try (LatencyWriter latencyWriter = new LatencyWriter(path)) {
            EncodableHistogram histogram = latencyRecorder.getIntervalHistogram();

            String camelVersion = System.getProperty("camel.version", "3.x.x");
            histogram.setTag(camelVersion);
            latencyWriter.outputIntervalHistogram(histogram);
        } catch (IOException e) {
            System.err.println("Unable to save latency file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static RouteBuilder getDataSetThreadedProcessor(LongAdder longAdder) {
        int threadCount = threadCount();
        return new DataSetThreadedProcessor(longAdder, threadCount);
    }

    private static RouteBuilder getDataSetNoopToDirect() {
        int threadCount = threadCount();
        return new DataSetNoopToDirect(threadCount);
    }

    private static RouteBuilder getDataSetNoopToSeda() {
        int threadCount = threadCount();
        return new DataSetNoopToSeda(threadCount);
    }

    private static RouteBuilder getThreadedProducerTemplate() {
        int threadCount = threadCount();
        return new ThreadedProducerTemplate(threadCount);
    }

    private static void bindDataSet(Main main, int testSize) {
        SimpleDataSet simpleDataSet = new SimpleDataSet();

        simpleDataSet.setDefaultBody(Boolean.TRUE);
        simpleDataSet.setSize(testSize / 2);
        simpleDataSet.setDefaultBody("{\"value\":\"data\"}");

        main.bind("testSet", simpleDataSet);
    }
}

