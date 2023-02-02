package org.apache.camel.controller.common.types;

public class Header {
    private String formatVersion;
    private String testerVersion;

    public String getFormatVersion() {
        return formatVersion;
    }

    public void setFormatVersion(String formatVersion) {
        this.formatVersion = formatVersion;
    }

    public String getTesterVersion() {
        return testerVersion;
    }

    public void setTesterVersion(String testerVersion) {
        this.testerVersion = testerVersion;
    }
}
