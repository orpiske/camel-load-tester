package org.apache.camel.kafka.tester;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.CamelContext;
import org.apache.camel.main.BaseMainSupport;
import org.apache.camel.main.MainListener;

public class TestMainListener implements MainListener {
    @FunctionalInterface
    interface Action {
        void execute();
    }

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    private final BufferedWriter bw;
    private final LongAdder longAdder;
    private final long testSize;
    private final Action staleAction;


    private volatile long lastCount = 0;
    private volatile long lastReportedCount = 0;
    private volatile long staleCounter = 0;


    public TestMainListener(BufferedWriter bw, LongAdder longAdder, long testSize, Action stopAction) {
        this.bw = bw;
        this.longAdder = longAdder;
        this.testSize = testSize;
        this.staleAction = stopAction;
    }

    @Override
    public void afterConfigure(BaseMainSupport main) {
        // NO-OP
    }

    @Override
    public void afterStart(BaseMainSupport main) {
        executorService.scheduleAtFixedRate(() -> {
            if (skipExceededCount()) {
                return;
            }

            lastReportedCount = lastCount;
            lastCount = measureAndWrite(bw, longAdder, lastCount);
        },
        1, 1, TimeUnit.SECONDS);
    }

    private boolean skipExceededCount() {
        if (lastCount >= testSize) {
            System.out.println("Processed more messages than setup: " + lastCount + " > "
                    + testSize);

            return true;
        }

        return false;
    }

    @Override
    public void afterStop(BaseMainSupport main) {
        if (!skipExceededCount()) {
            lastCount = measureAndWrite(bw, longAdder, lastCount);
        }

        save(bw);

        System.out.printf("Processed %d messages%n", lastCount);
    }

    @Override
    public void beforeConfigure(BaseMainSupport main) {
        // NO-OP
    }

    @Override
    public void beforeInitialize(BaseMainSupport main) {
        // NO-OP
    }

    @Override
    public void beforeStart(BaseMainSupport main) {
        executorService.scheduleWithFixedDelay(() -> {
            save(bw);
            progress(testSize, lastCount);

            staledCheck();

        }, 10, 10, TimeUnit.SECONDS);
    }

    private void staledCheck() {
        if (lastReportedCount == lastCount) {
            staleCounter++;
        } else {
            staleCounter = 0;
        }

        if (staleCounter > 6) {
            System.out.println("The test is stalled for too long");
            staleAction.execute();
        }
    }

    @Override
    public void beforeStop(BaseMainSupport main) {
        executorService.shutdown();
    }

    @Override
    public void configure(CamelContext context) {
        // NO-OP

    }

    private static void save(BufferedWriter bw) {
        try {
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static long measureAndWrite(BufferedWriter bw, LongAdder longAdder, long lastCount) {
        long currentCount = longAdder.longValue();
        long difference = currentCount - lastCount;

        try {
            bw.write(String.valueOf(difference));
            bw.newLine();

            return currentCount;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static void progress(long testSize, long lastCount) {
        long delta = (testSize - lastCount);
        double progress = 100.0 * ((double) delta / (double) testSize);
        System.out.printf("Remaining: %.5f%% (%d of %d) - %d messages to finish%n", progress, lastCount, testSize, delta);
    }
}
