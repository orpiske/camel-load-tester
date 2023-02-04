package org.apache.camel.kafka.tester.output;

import org.HdrHistogram.Histogram;
import org.apache.camel.kafka.tester.RateData;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleOutputHandler implements OutputHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ConsoleOutputHandler.class);

    private void logDeltas(double delta, String name) {
        if (delta < 0) {
            LOG.warn("The {} was lower for the test than for the baseline", name);
        } else {
            LOG.info("The {} was greater for the test than for the baseline", name);
        }
    }

    @Override
    public void outputSingleAnalyzis(RateData testData, SummaryStatistics testStatistics) {
        LOG.info("Test suite version: {}", testData.header.getCamelVersion());
        LOG.info("Version: {}", testData.header.getFileVersion());
        LOG.info("Type: {}", testData.header.getRole().name());
        LOG.info("Total: {}", testStatistics.getSum());
        LOG.info("Minimum: {}", testStatistics.getMin());
        LOG.info("Maximum: {}", testStatistics.getMax());
        LOG.info("Mean: {}", testStatistics.getMean());
        LOG.info("Geometric mean: {}", testStatistics.getGeometricMean());
        LOG.info("Standard deviation: {}", testStatistics.getStandardDeviation());
    }

    @Override
    public void outputWithBaseline(RateData testData, SummaryStatistics testStatistics, RateData baselineData, SummaryStatistics baselineStatistics) {
        LOG.info("TEST INFO:");
        LOG.info("Test suite version: {}", testData.header.getCamelVersion());
        LOG.info("Version: {}", testData.header.getFileVersion());
        LOG.info("Type: {}", testData.header.getRole().name());
        LOG.info("Test version: {} / Baseline version: {}", testData.header.getCamelVersion(), baselineData.header.getCamelVersion());

        LOG.info("");
        LOG.info("TOTAL EXCHANGES:");
        LOG.info("Test: {} | Baseline: {}", testStatistics.getSum(), baselineStatistics.getSum());

        double totalDelta = testStatistics.getSum() - baselineStatistics.getSum();
        LOG.info("Delta: {}", totalDelta);
        logDeltas(totalDelta, "total number of exchanges");

        LOG.info("");
        LOG.info("MINIMUM RATE");
        LOG.info("Test: {} | Baseline: {}", testStatistics.getMin(), baselineStatistics.getMin());
        final double minDelta = testStatistics.getMin() - baselineStatistics.getMin();
        LOG.info("Delta: {}", minDelta);
        logDeltas(minDelta, "minimum rate");

        LOG.info("");
        LOG.info("MAXIMUM RATE:");
        LOG.info("Test: {} | Baseline: {}", testStatistics.getMax(), baselineStatistics.getMax());

        final double maxDelta = testStatistics.getMax() - baselineStatistics.getMax();
        LOG.info("Delta: {}", maxDelta);
        logDeltas(maxDelta, "maximum rate");

        LOG.info("");
        LOG.info("MEAN (RATE):");
        LOG.info("Test: {} | Baseline: {}", testStatistics.getMean(), baselineStatistics.getMean());

        final double meanDelta = testStatistics.getMean() - baselineStatistics.getMean();
        LOG.info("Delta: {}", meanDelta);
        logDeltas(meanDelta, "mean rate");

        LOG.info("");
        LOG.info("GEOMETRIC MEAN (RATE):");
        LOG.info("Test: {} | Baseline: {}", testStatistics.getGeometricMean(), baselineStatistics.getGeometricMean());

        final double geoMeanDelta = testStatistics.getGeometricMean() - baselineStatistics.getGeometricMean();
        LOG.info("Delta: {}", geoMeanDelta);
        logDeltas(geoMeanDelta, "geometric mean rate");

        LOG.info("");
        LOG.info("OTHER:");
        LOG.info("Test standard deviation: {} | Baseline standard deviation: {}", testStatistics.getStandardDeviation(), baselineStatistics.getStandardDeviation());
    }

    @Override
    public void outputHistogram(Histogram histogram) {
        LOG.info("Latency start time: {}", histogram.getStartTimeStamp());
        LOG.info("Latency end time: {}", histogram.getEndTimeStamp());
        LOG.info("Latency max value: {}", histogram.getMaxValue());
        LOG.info("p50 (median) latency: {}", histogram.getValueAtPercentile(50.0));
        LOG.info("p90 latency: {}", histogram.getValueAtPercentile(90.0));
        LOG.info("p95 latency: {}", histogram.getValueAtPercentile(95.0));
        LOG.info("p99 latency: {}", histogram.getValueAtPercentile(99.0));
        LOG.info("p99.9 latency: {}", histogram.getValueAtPercentile(99.9));
    }

    @Override
    public void outputHistogram(Histogram histogram, Histogram baseline) {
        LOG.info("LATENCY INFO:");
        LOG.info("Latency start time: {}", histogram.getStartTimeStamp());
        LOG.info("Latency end time: {}", histogram.getEndTimeStamp());
        LOG.info("Latency max value: {}", histogram.getMaxValue());

        LOG.info("p50 (median) latency: {} / baseline: {}", histogram.getValueAtPercentile(50.0), baseline.getValueAtPercentile(50.0));

        double p50Delta = histogram.getValueAtPercentile(50.0) - baseline.getValueAtPercentile(50.0);
        logDeltas(p50Delta, "p50 (median) latency");

        LOG.info("p90 latency: {} / baseline: {}", histogram.getValueAtPercentile(90.0), baseline.getValueAtPercentile(90.0));
        double p90Delta = histogram.getValueAtPercentile(90.0) - baseline.getValueAtPercentile(90.0);
        logDeltas(p90Delta, "p90 latency");

        LOG.info("p95 latency: {} / baseline: {}", histogram.getValueAtPercentile(95.0), baseline.getValueAtPercentile(95.0));
        double p95Delta = histogram.getValueAtPercentile(95.0) - baseline.getValueAtPercentile(95.0);
        logDeltas(p95Delta, "p95 latency");

        LOG.info("p99 latency: {} / baseline: {}", histogram.getValueAtPercentile(99.0), baseline.getValueAtPercentile(99.0));
        double p99Delta = histogram.getValueAtPercentile(99.0) - baseline.getValueAtPercentile(99.0);
        logDeltas(p99Delta, "p99 latency");

        LOG.info("p99.9 latency: {} / baseline: {}", histogram.getValueAtPercentile(99.9), baseline.getValueAtPercentile(99.9));
        double p999Delta = histogram.getValueAtPercentile(99.9) - baseline.getValueAtPercentile(99.9);
        logDeltas(p999Delta, "p99.9 latency");
    }
}
