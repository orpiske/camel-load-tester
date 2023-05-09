package org.apache.camel.kafka.tester.common.types;

public class BaselinedTestMetrics implements TestResult {
    private TestMetrics testMetrics;
    private TestMetrics baselineMetrics;

    public TestMetrics getTestMetrics() {
        return testMetrics;
    }

    public void setTestMetrics(TestMetrics testMetrics) {
        this.testMetrics = testMetrics;
    }

    public TestMetrics getBaselineMetrics() {
        return baselineMetrics;
    }

    public void setBaselineMetrics(TestMetrics baselineMetrics) {
        this.baselineMetrics = baselineMetrics;
    }
}
