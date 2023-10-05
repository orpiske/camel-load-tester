package org.apache.camel.load.tester.common;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

import org.apache.camel.load.tester.io.RateWriter;
import org.apache.camel.load.tester.io.RecordState;

public class WriterReporter extends AbstractReporter {

    private final RateWriter writer;
    private Consumer<Long> onComplete;

    public WriterReporter(RateWriter writer, long testSize, Reporter.Action staleAction, Consumer<Long> onComplete) {
        super(testSize, staleAction, Counter.getInstance().getAdder());
        this.writer = writer;
        this.onComplete = onComplete;
    }

    protected void update(LongAdder longAdder) {
        long difference = measure(longAdder);

        try {
            final Instant now = Instant.now();
            RecordState state = writer.write(0, difference, now);
            if (state == RecordState.OUTDATED) {
                LOG.error("Sequential record with a timestamp in the past: {}", now);
            } else if (state == RecordState.DUPLICATED) {
                LOG.error("Multiple records for within the same second slot: {}", now);
            }
        } catch (IOException e) {
            LOG.error("Unable to update records: {}", e.getMessage());
        }
    }

    @Override
    public void closeReport() {
        if (!skipExceededCount()) {
            long difference = measure(longAdder);

            try {
                writer.tryWrite(0, difference, Instant.now());
                LOG.info("Processed {} messages", lastCount);
            } catch (IOException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.error("Unable to perform the last update: {}", e.getMessage(), e);
                } else {
                    LOG.error("Unable to perform the last update: {}", e.getMessage());
                }
            } finally {
                if (onComplete != null) {
                    onComplete.accept(lastCount);
                }
            }
        }
    }

}