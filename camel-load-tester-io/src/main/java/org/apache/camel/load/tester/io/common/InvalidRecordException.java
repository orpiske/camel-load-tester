package org.apache.camel.load.tester.io.common;

public class InvalidRecordException extends RuntimeException {
    private final long now;
    private final long last;


    public InvalidRecordException(long now, long last, String message) {
        super(message);

        this.now = now;
        this.last = last;
    }

    public long getNow() {
        return now;
    }

    public long getLast() {
        return last;
    }
}
