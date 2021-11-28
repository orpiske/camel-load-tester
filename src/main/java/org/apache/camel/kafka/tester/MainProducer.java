package org.apache.camel.kafka.tester;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.component.dataset.SimpleDataSet;
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

        String name = System.getProperty("test.file", "test.data");

        LongAdder longAdder = new LongAdder();
        int testSize = Integer.parseInt(System.getProperty("camel.main.durationMaxMessages", "0"));

        SimpleDataSet simpleDataSet = new SimpleDataSet();

        simpleDataSet.setDefaultBody("test");
        simpleDataSet.setSize(testSize);

        main.bind("testSet", simpleDataSet);

        int batchSize = Integer.parseInt(System.getProperty("test.batch.size", "0"));

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(name))) {

            if (batchSize > 0) {
                main.configure().addRoutesBuilder(new MyProducer(longAdder, true, batchSize));
            } else {
                main.configure().addRoutesBuilder(new MyProducer(longAdder, false, 0));
            }

            main.addMainListener(new TestMainListener(bw, longAdder, testSize, main::stop));

            main.run();
        }

    }
}

