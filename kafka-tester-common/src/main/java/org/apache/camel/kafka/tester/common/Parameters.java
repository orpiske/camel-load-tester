package org.apache.camel.kafka.tester.common;

public final class Parameters {

    public static final String TEST_BATCH_SIZE = "test.batch.size";
    public static final String TEST_KAFKA_TOPIC = "kafka.topic";
    public static final String TEST_LATENCIES_FILE = "test.latencies.file";
    public static final String CAMEL_VERSION = "camel.version";
    public static final String TEST_RATE_FILE = "test.rate.file";
    public static final String CAMEL_MAIN_DURATION_MAX_MESSAGES = "camel.main.durationMaxMessages";
    public static final String TEST_PRODUCER_TYPE = "test.producer.type";
    public static final String TEST_THREAD_COUNT = "test.thread.count";

    private Parameters() {

    }

    public static int threadCount() {
        String strThreadCount = System.getProperty(TEST_THREAD_COUNT, "1");
        if (strThreadCount.equals("max")) {
            return Runtime.getRuntime().availableProcessors();
        }

        return Integer.parseInt(strThreadCount);
    }

    public static int batchSize() {
        return Integer.parseInt(System.getProperty(Parameters.TEST_BATCH_SIZE, "1"));
    }

    public static String kafkaTopic() {
        return System.getProperty(Parameters.TEST_KAFKA_TOPIC, "test-topic-producer");
    }
}
