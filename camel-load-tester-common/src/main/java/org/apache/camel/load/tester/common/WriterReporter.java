package org.apache.camel.load.tester.common;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

import org.apache.camel.load.tester.io.RateWriter;

public class WriterReporter extends AbstractReporter {

    private final RateWriter writer;
    private Consumer<Long> onComplete;

    public WriterReporter(RateWriter writer, long testSize, WriterReporter.Action staleAction, Consumer<Long> onComplete) {
        super(testSize, staleAction, Counter.getInstance().getAdder());
        this.writer = writer;
        this.onComplete = onComplete;
    }

    protected void update(LongAdder longAdder) {
        long difference = measure(longAdder);

        try {
            writer.write(0, difference, Instant.now());
        } catch (Exception e) {
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
