package org.apache.camel.controller.common.types;

public class CamelMeta {
    private String camelVersion;
    private String baselineVersion;

    public String getCamelVersion() {
        return camelVersion;
    }

    public void setCamelVersion(String camelVersion) {
        this.camelVersion = camelVersion;
    }

    public String getBaselineVersion() {
        return baselineVersion;
    }

    public void setBaselineVersion(String baselineVersion) {
        this.baselineVersion = baselineVersion;
    }
}
