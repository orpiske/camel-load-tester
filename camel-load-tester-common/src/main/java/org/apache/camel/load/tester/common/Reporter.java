package org.apache.camel.load.tester.common;

public interface Reporter {
    void update();

    void closeReport();

    void staledCheck();

    @FunctionalInterface
    interface Action {
        void execute();
    }
}
