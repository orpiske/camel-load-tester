package org.apache.camel.kafka.tester.io.common;

public class RecordOverwriteException extends InvalidRecordException {
    public RecordOverwriteException(long now, long last, String message) {
        super(now, last, message);
    }
}
