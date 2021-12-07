package org.apache.camel.kafka.tester;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.kafka.tester.io.writer.RateWriter;

public class Reporter {
    @FunctionalInterface
    interface Action {
        void execute();
    }

    private long lastCount = 0;
    private long lastReportedCount = 0;
    private long staleCounter = 0;

    private final RateWriter writer;
    private final long testSize;
    private final Action staleAction;
    private final LongAdder longAdder;

    public Reporter(RateWriter writer, LongAdder longAdder, long testSize, Reporter.Action staleAction) {
        this.writer = writer;
        this.testSize = testSize;
        this.longAdder = longAdder;
        this.staleAction = staleAction;
    }

    private void update(LongAdder longAdder) {
        long difference = measure(longAdder);

        try {
            writer.write(0, difference, Instant.now());
        } catch (Exception e) {
            System.err.println("Unable to update records: " + e.getMessage());
        }
    }

    private long measure(LongAdder longAdder) {
        long currentCount = longAdder.longValue();
        long difference = currentCount - lastCount;
        lastCount = currentCount;

        return difference;
    }

    private boolean skipExceededCount() {
        if (lastCount >= testSize) {
            System.out.println("Processed more messages than setup: " + lastCount + " > "
                    + testSize);

            return true;
        }

        return false;
    }

    private void progress(long testSize) {
        long delta = (testSize - lastCount);
        double progress = 100.0 * ((double) delta / (double) testSize);
        System.out.printf("Remaining: %.5f%% (%d of %d) - %d messages to finish%n", progress, lastCount, testSize, delta);
    }

    public void update() {
        if (skipExceededCount()) {
            return;
        }

        lastReportedCount = lastCount;
        update(longAdder);
    }

    public void closeReport() {
        if (!skipExceededCount()) {
            long difference = measure(longAdder);

            try {
                writer.tryWrite(0, difference, Instant.now());
                System.out.printf("Processed %d messages%n", lastCount);
            } catch (IOException e) {
                System.err.println("Unable to perform the last update: " + e.getMessage());
            }
        }
    }

    public void staledCheck() {
        if (lastReportedCount == lastCount) {
            staleCounter++;
        } else {
            staleCounter = 0;
        }

        progress(testSize);

        if (staleCounter > 6) {
            System.out.println("The test is stalled for too long");
            staleAction.execute();
        }
    }

}
