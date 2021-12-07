package org.apache.camel.kafka.tester.io.writer;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

public interface RateWriter extends AutoCloseable {
    File reportFile();

    void write(int metadata, long count, long timestamp) throws IOException;

    void write(int metadata, long count, Instant instant) throws IOException;

    void tryWrite(int metadata, long count, Instant instant) throws IOException;
}
