package org.apache.camel.load.tester.io.common;

public class RecordOverwriteException extends InvalidRecordException {
    public RecordOverwriteException(long now, long last, String message) {
        super(now, last, message);
    }
}
