package org.apache.camel.load.tester.io;

import java.io.IOException;
import java.time.Instant;

public interface RateWriter extends AutoCloseable {

    RecordState write(int metadata, long count, long timestamp) throws IOException;

    RecordState write(int metadata, long count, Instant instant) throws IOException;

    RecordState tryWrite(int metadata, long count, Instant instant) throws IOException;
}
