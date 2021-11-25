package org.apache.camel.kafka.tester;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.CamelContext;
import org.apache.camel.main.BaseMainSupport;
import org.apache.camel.main.Main;
import org.apache.camel.main.MainListener;

/**
 * A Camel Application
 */
public class MainConsumer {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
        Main main = new Main();

        String name = System.getProperty("test.file", "test.data");

        LongAdder longAdder = new LongAdder();
        long testSize = Long.parseLong(System.getProperty("camel.main.durationMaxMessages", "0"));


        try (BufferedWriter bw = new BufferedWriter(new FileWriter(name))) {
            main.configure().addRoutesBuilder(new MyConsumer(longAdder));
            main.addMainListener(new TestMainListener(bw, longAdder, testSize, main::stop));
            main.run(args);
        }

    }



}

