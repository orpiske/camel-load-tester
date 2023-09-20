package org.apache.camel.load.tester.support;

public class Sample {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

// This is cheating: makes a converter to be registered and things to run much faster ;)
//    @Override
//    public String toString() {
//        return value;
//    }
}
