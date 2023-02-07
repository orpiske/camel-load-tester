package org.apache.camel.controller.common.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.camel.kafka.tester.common.types.BaselinedTestMetrics;
import org.apache.camel.kafka.tester.common.types.TestMetrics;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestExecution {
    private String id;
    private Header header;
    private CamelMeta camelMeta;
    private TestDuration testDuration;
    private TestState testState;
    private String tester;
    private String testName;
    private String testType;
    private String testerArguments;
    private int timeout;
    private TestMetrics testMetrics;
    private BaselinedTestMetrics baselinedTestMetrics;

    public String getTesterArguments() {
        return testerArguments;
    }

    public void setTesterArguments(String testerArguments) {
        this.testerArguments = testerArguments;
    }

    public String getTester() {
        return tester;
    }

    public void setTester(String tester) {
        this.tester = tester;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public CamelMeta getCamelMeta() {
        return camelMeta;
    }

    public void setCamelMeta(CamelMeta camelMeta) {
        this.camelMeta = camelMeta;
    }

    public TestDuration getTestDuration() {
        return testDuration;
    }

    public void setTestDuration(TestDuration testDuration) {
        this.testDuration = testDuration;
    }

    public TestState getTestState() {
        return testState;
    }

    public void setTestState(TestState testState) {
        this.testState = testState;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public TestMetrics getTestMetrics() {
        return testMetrics;
    }

    public void setTestMetrics(TestMetrics testMetrics) {
        this.testMetrics = testMetrics;
    }

    public BaselinedTestMetrics getBaselinedTestMetrics() {
        return baselinedTestMetrics;
    }

    public void setBaselinedTestMetrics(BaselinedTestMetrics baselinedTestMetrics) {
        this.baselinedTestMetrics = baselinedTestMetrics;
    }
}
