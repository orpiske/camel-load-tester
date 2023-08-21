package org.apache.camel.load.tester.io.common;

public enum Role {
    CONSUMER(0),
    PRODUCER(1);

    private final int code;

    Role(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Role from(int value) {
        switch (value) {
            case 0: return CONSUMER;
            case 1: return PRODUCER;
            default: throw new AssertionError("Invalid value for role: " + value);
        }
    }
}
