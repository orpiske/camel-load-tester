package org.apache.camel.load.analyzer.output;

import java.util.HashSet;
import java.util.Set;

import org.apache.camel.load.tester.common.types.BaselinedTestMetrics;
import org.apache.camel.load.tester.common.types.TestMetrics;

public class DelegateOutputHandler implements OutputHandler {
    private Set<OutputHandler> outputHandlers = new HashSet<>();

    public void add(OutputHandler handler) {
        outputHandlers.add(handler);
    }

    @Override
    public void output(TestMetrics testMetrics) {
        for (var handler : outputHandlers) {
            handler.output(testMetrics);
        }
    }

    @Override
    public void output(BaselinedTestMetrics testMetrics) {
        for (var handler : outputHandlers) {
            handler.output(testMetrics);
        }
    }
}
