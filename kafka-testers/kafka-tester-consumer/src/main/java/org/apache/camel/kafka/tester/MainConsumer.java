package org.apache.camel.kafka.tester;

import java.io.File;
import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.kafka.tester.common.TestMainListener;
import org.apache.camel.kafka.tester.io.common.FileHeader;
import org.apache.camel.kafka.tester.io.BinaryRateWriter;
import org.apache.camel.kafka.tester.io.RateWriter;
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

        String name = System.getProperty("test.rate.file", "consumer-rate.data");

        LongAdder longAdder = new LongAdder();
        long testSize = Long.parseLong(System.getProperty("camel.main.durationMaxMessages", "0"));

        File reportFile = new File(name);
        try (RateWriter rateWriter = new BinaryRateWriter(reportFile, FileHeader.WRITER_DEFAULT_CONSUMER)) {
            main.configure().addRoutesBuilder(new TestConsumer(longAdder));
            main.addMainListener(new TestMainListener(rateWriter, longAdder, testSize, main::stop));
            main.run(args);
        }

    }



}

