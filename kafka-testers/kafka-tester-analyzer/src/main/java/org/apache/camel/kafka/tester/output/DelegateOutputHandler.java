package org.apache.camel.kafka.tester.output;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.HdrHistogram.Histogram;
import org.apache.camel.kafka.tester.common.types.BaselinedTestMetrics;
import org.apache.camel.kafka.tester.common.types.TestMetrics;

public class DelegateOutputHandler implements OutputHandler {
    private Set<OutputHandler> outputHandlers = new HashSet<>();

    public <T extends OutputHandler> void add(OutputHandler handler) {
        outputHandlers.add(handler);
    }

    @Override
    public void outputSingleAnalyzis(TestMetrics testMetrics) {
        for (var handler : outputHandlers) {
            handler.outputSingleAnalyzis(testMetrics);
        }
    }

    @Override
    public void outputWithBaseline(BaselinedTestMetrics testMetrics) {
        for (var handler : outputHandlers) {
            handler.outputWithBaseline(testMetrics);
        }
    }

    @Override
    public void outputHistogram(Histogram histogram) {
        for (var handler : outputHandlers) {
            handler.outputHistogram(histogram);
        }
    }

    @Override
    public void outputHistogram(BaselinedTestMetrics testMetrics) {
        for (var handler : outputHandlers) {
            handler.outputHistogram(testMetrics);
        }
    }
}
