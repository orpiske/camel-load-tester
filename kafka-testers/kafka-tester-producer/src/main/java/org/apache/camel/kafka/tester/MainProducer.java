package org.apache.camel.kafka.tester;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.HdrHistogram.EncodableHistogram;
import org.HdrHistogram.SingleWriterRecorder;
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

        final String topic = System.getProperty("kafka.topic", "test-topic-producer");
        final String testRateFileName = System.getProperty("test.rate.file", "producer-rate.data");

        final LongAdder longAdder = new LongAdder();
        int testSize = Integer.parseInt(System.getProperty("camel.main.durationMaxMessages", "0"));

        bindDataSet(main, testSize);

        int batchSize = Integer.parseInt(System.getProperty("test.batch.size", "0"));

        SingleWriterRecorder latencyRecorder = new SingleWriterRecorder(TimeUnit.HOURS.toMicros(1), 3);

        File testRateFile = new File(testRateFileName);
        try (RateWriter rateWriter = new BinaryRateWriter(testRateFile, FileHeader.WRITER_DEFAULT_PRODUCER)) {
            if (batchSize > 0) {
                main.configure().addRoutesBuilder(new TestNoopProducer(latencyRecorder, longAdder, true, batchSize, topic));
            } else {
                main.configure().addRoutesBuilder(new TestNoopProducer(latencyRecorder, longAdder, false, 0, topic));
            }

            main.addMainListener(new TestMainListener(rateWriter, longAdder, testSize, main::stop));

            main.run();
        }

        final String testLatenciesFileName = System.getProperty("test.latencies.file", "producer-latencies.hdr");
        try (LatencyWriter latencyWriter = new LatencyWriter(new File(testLatenciesFileName))) {
            EncodableHistogram histogram = latencyRecorder.getIntervalHistogram();

            String camelVersion = System.getProperty("camel.version", "3.x.x");
            histogram.setTag(camelVersion);
            latencyWriter.outputIntervalHistogram(histogram);
        }
    }

    private static void bindDataSet(Main main, int testSize) {
        SimpleDataSet simpleDataSet = new SimpleDataSet();

        simpleDataSet.setDefaultBody("test");
        simpleDataSet.setSize(testSize);

        main.bind("testSet", simpleDataSet);
    }
}

