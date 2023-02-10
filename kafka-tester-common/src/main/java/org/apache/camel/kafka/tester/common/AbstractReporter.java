package org.apache.camel.kafka.tester.common;

import java.util.concurrent.atomic.LongAdder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractReporter implements Reporter {
    protected static final Logger LOG = LoggerFactory.getLogger(WriterReporter.class);
    protected final long testSize;
    protected final Action staleAction;
    protected final LongAdder longAdder;
    protected long lastCount = 0;
    protected long lastReportedCount = 0;
    private long staleCounter = 0;

    public AbstractReporter(long testSize, Action staleAction, LongAdder longAdder) {
        this.testSize = testSize;
        this.staleAction = staleAction;
        this.longAdder = longAdder;
    }

    abstract protected void update(LongAdder longAdder);

    protected long measure(LongAdder longAdder) {
        long currentCount = longAdder.longValue();
        long difference = currentCount - lastCount;
        lastCount = currentCount;

        return difference;
    }

    protected boolean skipExceededCount() {
        if (lastCount >= testSize) {
            LOG.warn("Processed more messages than setup: {} > {}", lastCount, testSize);

            return true;
        }

        return false;
    }

    private void progress(long testSize) {
        long delta = (testSize - lastCount);
        double progress = 100.0 * ((double) delta / (double) testSize);
        String message = String.format("Remaining: %.5f%% (%d of %d) - %d messages to finish", progress, lastCount, testSize, delta);

        LOG.info(message);

        double rate = (longAdder.longValue() - lastCount) / 10.0;
        String rateMessage = String.format("Current rate: %.5f exchanges/sec", rate);
        LOG.info(rateMessage);
    }

    @Override
    public void staledCheck() {
        if (lastReportedCount == lastCount) {
            staleCounter++;
        } else {
            staleCounter = 0;
        }

        progress(testSize);

        if (staleCounter > 6) {
            LOG.warn("The test is stalled for too long");
            staleAction.execute();
        }
    }

    @Override
    public void update() {
        if (skipExceededCount()) {
            return;
        }

        lastReportedCount = lastCount;
        update(longAdder);
    }
}
