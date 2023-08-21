package org.apache.camel.load.tester;

import java.io.File;
import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.load.tester.common.IOUtil;
import org.apache.camel.load.tester.common.TestMainListener;
import org.apache.camel.load.tester.common.WriterReporter;
import org.apache.camel.load.tester.io.common.FileHeader;
import org.apache.camel.load.tester.io.BinaryRateWriter;
import org.apache.camel.load.tester.io.RateWriter;
import org.apache.camel.main.Main;

/**
 * A Camel Application
 */
public class MainConsumer {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
        Main main = new Main();

        final String topic = System.getProperty("kafka.topic", "test-topic-consumer");
        String name = System.getProperty("test.rate.file", "consumer-rate.data");

        LongAdder longAdder = new LongAdder();
        long testSize = Long.parseLong(System.getProperty("camel.main.durationMaxMessages", "0"));

        File reportFile = IOUtil.create(name);

        try (RateWriter rateWriter = new BinaryRateWriter(reportFile, FileHeader.WRITER_DEFAULT_CONSUMER)) {
            main.configure().addRoutesBuilder(new TestConsumer(longAdder, topic));
            WriterReporter writerReporter = new WriterReporter(rateWriter, testSize, main::stop, null);

            main.addMainListener(new TestMainListener(writerReporter));
            main.run(args);
        }

    }



}

