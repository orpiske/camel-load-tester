package org.apache.camel.load.tester;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.apache.camel.main.Main;

/**
 * A Camel Application
 */
public class MainConsumer {
    private static final CountDownLatch LATCH = new CountDownLatch(1);
    private static final Main MAIN = new Main();

    private static void shutdownExecutor() {
        try {
            LATCH.await();
        } catch (InterruptedException e) {
            // Ignored ... shutting down
        }

        MAIN.stop();

        System.exit(0);
    }

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
        Executors.newFixedThreadPool(1).submit(() -> shutdownExecutor());

        MAIN.configure().addRoutesBuilder(new PlatformHTTPConsumerRoute(LATCH));
        MAIN.run(args);
    }



}

