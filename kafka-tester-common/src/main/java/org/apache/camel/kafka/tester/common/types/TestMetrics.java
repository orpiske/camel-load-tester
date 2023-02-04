package org.apache.camel.kafka.tester.common.types;

public class TestMetrics {
    private int testSuiteVersion;
    private int sutVersion;
    private String type;
    private Metrics testMetrics;

    public int getTestSuiteVersion() {
        return testSuiteVersion;
    }

    public void setTestSuiteVersion(int testSuiteVersion) {
        this.testSuiteVersion = testSuiteVersion;
    }

    public int getSutVersion() {
        return sutVersion;
    }

    public void setSutVersion(int sutVersion) {
        this.sutVersion = sutVersion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Metrics getTestMetrics() {
        return testMetrics;
    }

    public void setTestMetrics(Metrics testMetrics) {
        this.testMetrics = testMetrics;
    }
}
