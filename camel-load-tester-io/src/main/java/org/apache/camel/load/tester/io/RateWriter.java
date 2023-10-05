package org.apache.camel.load.tester.io;

import java.io.IOException;
import java.time.Instant;

public interface RateWriter extends AutoCloseable {

    void write(int metadata, long count, long timestamp) throws IOException;

    void write(int metadata, long count, Instant instant) throws IOException;

    void tryWrite(int metadata, long count, Instant instant) throws IOException;
}
