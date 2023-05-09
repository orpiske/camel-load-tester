package org.apache.camel.kafka.tester.output;

import org.apache.camel.kafka.tester.common.types.BaselinedTestMetrics;
import org.apache.camel.kafka.tester.common.types.TestMetrics;

public interface OutputHandler {

    void output(TestMetrics testMetrics);
    void output(BaselinedTestMetrics testMetrics);
}
