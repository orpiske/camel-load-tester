package org.apache.camel.kafka.tester.common;

public interface Reporter {
    void update();

    void closeReport();

    void staledCheck();

    @FunctionalInterface
    interface Action {
        void execute();
    }
}
