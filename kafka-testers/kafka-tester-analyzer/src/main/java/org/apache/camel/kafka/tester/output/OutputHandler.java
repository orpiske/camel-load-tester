package org.apache.camel.kafka.tester.output;

import org.HdrHistogram.Histogram;
import org.apache.camel.kafka.tester.RateData;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public interface OutputHandler {

    void outputSingleAnalyzis(RateData testData, SummaryStatistics testStatistics);
    void outputWithBaseline(RateData testData, SummaryStatistics testStatistics, RateData baselineData, SummaryStatistics baselineStatistics);

    void outputHistogram(Histogram histogram);
    void outputHistogram(Histogram histogram, Histogram baseline);
}
