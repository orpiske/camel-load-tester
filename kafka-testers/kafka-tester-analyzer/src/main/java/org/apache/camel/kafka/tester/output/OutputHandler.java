package org.apache.camel.kafka.tester.output;

import org.HdrHistogram.Histogram;
import org.apache.camel.kafka.tester.RateData;
import org.apache.camel.kafka.tester.common.types.BaselinedTestMetrics;
import org.apache.camel.kafka.tester.common.types.TestMetrics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public interface OutputHandler {

    void outputSingleAnalyzis(TestMetrics testMetrics);
    void outputWithBaseline(BaselinedTestMetrics testMetrics);

    void outputHistogram(Histogram histogram);
    void outputHistogram(Histogram histogram, Histogram baseline);
}
