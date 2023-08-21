package org.apache.camel.load.tester.common.types;

public class TestMetrics implements TestResult {
    private int testSuiteVersion;
    private int sutVersion;
    private String type;
    private Metrics metrics;

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

    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }
}
