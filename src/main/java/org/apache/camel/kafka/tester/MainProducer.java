package org.apache.camel.kafka.tester;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.component.dataset.ListDataSet;
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

        List<Object> dataList = new ArrayList<>(testSize);
        String testData = "test";

        for (int i = 0; i < testSize; i++) {
            dataList.add(testData + "-" + i);
        }

        ListDataSet listDataSet = new ListDataSet(dataList);

        main.bind("testSet", listDataSet);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(name))) {
            main.configure().addRoutesBuilder(new MyProducer(longAdder));
            main.addMainListener(new TestMainListener(bw, longAdder, testSize, main::stop));

            main.run();
        }

    }
}

