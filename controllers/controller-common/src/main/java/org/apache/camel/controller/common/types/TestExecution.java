package org.apache.camel.controller.common.types;

public class TestExecution {
    private String id;
    private Header header;
    private CamelMeta camelMeta;
    private TestDuration testDuration;
    private TestState testState;

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
}
