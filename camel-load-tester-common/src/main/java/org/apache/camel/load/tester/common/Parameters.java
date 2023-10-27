package org.apache.camel.load.tester.common;

public final class Parameters {

    public static final String TEST_BATCH_SIZE = "test.batch.size";
    public static final String TEST_KAFKA_TOPIC = "kafka.topic";
    public static final String TEST_LATENCIES_FILE = "test.latencies.file";
    public static final String CAMEL_VERSION = "camel.version";
    public static final String TEST_RATE_FILE = "test.rate.file";
    public static final String CAMEL_MAIN_DURATION_MAX_MESSAGES = "camel.main.durationMaxMessages";
    public static final String TEST_PRODUCER_TYPE = "test.producer.type";
    public static final String TEST_THREAD_COUNT_PROCESSOR = "test.thread.count.processor";
    public static final String TEST_THREAD_COUNT_CONSUMER = "test.thread.count.consumer";

    public static final String TEST_THREAD_COUNT_PRODUCER = "test.thread.count.producer";
    public static final String TEST_TARGET_RATE = "test.target.rate";
    public static final String TEST_ON_COMPLETE_ACTION = "test.on.complete.action";

    public static final String HTTP_PORT_PARAMETER = "test.consumer.http.port";

    private Parameters() {

    }

    private static int threadCountFromProperty(String testThreadCount) {
        String strThreadCount = System.getProperty(testThreadCount, "1");
        if (strThreadCount.equals("max")) {
            return Runtime.getRuntime().availableProcessors();
        }

        return Integer.parseInt(strThreadCount);
    }

    @Deprecated
    public static int threadCount() {
        return threadCountConsumer();
    }

    public static int threadCountProcessor() {
        return threadCountFromProperty(TEST_THREAD_COUNT_PROCESSOR);
    }

    public static int threadCountConsumer() {
        return threadCountFromProperty(TEST_THREAD_COUNT_CONSUMER);
    }

    public static int threadCountProducer() {
        return threadCountFromProperty(TEST_THREAD_COUNT_PRODUCER);
    }

    public static int batchSize() {
        return Integer.parseInt(System.getProperty(Parameters.TEST_BATCH_SIZE, "1"));
    }

    public static String kafkaTopic() {
        return System.getProperty(Parameters.TEST_KAFKA_TOPIC, "test-topic-producer");
    }

    public static int duration() {
        return Integer.parseInt(System.getProperty("camel.main.durationMaxMessages", "0"));
    }

    public static int targetRate() {
        return Integer.parseInt(System.getProperty(Parameters.TEST_TARGET_RATE, "0"));
    }

    public static int httpPortConsumer() {
        return Integer.parseInt(System.getProperty(HTTP_PORT_PARAMETER, "8080"));
    }
}
