package org.apache.camel.kafka.tester.common;

public final class Parameters {

    private Parameters() {

    }

    public static int threadCount() {
        String strThreadCount = System.getProperty("test.thread.count", "1");
        if (strThreadCount.equals("max")) {
            return Runtime.getRuntime().availableProcessors();
        }

        return Integer.parseInt(strThreadCount);
    }
}
