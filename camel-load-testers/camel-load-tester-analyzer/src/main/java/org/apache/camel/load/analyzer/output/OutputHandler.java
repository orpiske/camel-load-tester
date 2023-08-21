package org.apache.camel.load.analyzer.output;

import org.apache.camel.load.tester.common.types.BaselinedTestMetrics;
import org.apache.camel.load.tester.common.types.TestMetrics;

public interface OutputHandler {

    void output(TestMetrics testMetrics);
    void output(BaselinedTestMetrics testMetrics);
}
