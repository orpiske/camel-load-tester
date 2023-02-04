package org.apache.camel.kafka.tester.output;

import java.util.function.Supplier;

import org.HdrHistogram.Histogram;
import org.apache.camel.kafka.tester.common.types.BaselinedTestMetrics;
import org.apache.camel.kafka.tester.common.types.TestMetrics;
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
    public void outputSingleAnalyzis(TestMetrics testMetrics) {
        LOG.info("Test suite version: {}", testMetrics.getTestSuiteVersion());
        LOG.info("Type: {}", testMetrics.getType());
        LOG.info("Total: {}", testMetrics.getMetrics().getTotal());
        LOG.info("Minimum: {}", testMetrics.getMetrics().getMinimum());
        LOG.info("Maximum: {}", testMetrics.getMetrics().getMaximum());
        LOG.info("Mean: {}", testMetrics.getMetrics().getMean());
        LOG.info("Geometric mean: {}", testMetrics.getMetrics().getGeoMean());
        LOG.info("Standard deviation: {}", testMetrics.getMetrics().getStdDeviation());
    }


    @Override
    public void outputWithBaseline(BaselinedTestMetrics baselinedTestMetrics) {
        final TestMetrics testMetrics = baselinedTestMetrics.getTestMetrics();
        final TestMetrics baselineMetrics = baselinedTestMetrics.getBaselineMetrics();

        LOG.info("TEST INFO:");
        LOG.info("Test suite version: {}", testMetrics.getTestSuiteVersion());
        LOG.info("Type: {}", testMetrics.getType());
        LOG.info("Test version: {} / Baseline version: {}", testMetrics.getSutVersion(), baselineMetrics.getSutVersion());

        LOG.info("");
        LOG.info("TOTAL EXCHANGES:");
        LOG.info("Test: {} | Baseline: {}", testMetrics.getMetrics().getTotal(), baselineMetrics.getMetrics().getTotal());

        double totalDelta = testMetrics.getMetrics().getTotal() - baselineMetrics.getMetrics().getTotal();
        LOG.info("Delta: {}", totalDelta);
        logDeltas(totalDelta, "total number of exchanges");

        LOG.info("");
        LOG.info("MINIMUM RATE");
        LOG.info("Test: {} | Baseline: {}", testMetrics.getMetrics().getMinimum(), baselineMetrics.getMetrics().getMinimum());
        final double minDelta = testMetrics.getMetrics().getMinimum() - baselineMetrics.getMetrics().getMinimum();
        LOG.info("Delta: {}", minDelta);
        logDeltas(minDelta, "minimum rate");

        LOG.info("");
        LOG.info("MAXIMUM RATE:");
        LOG.info("Test: {} | Baseline: {}", testMetrics.getMetrics().getMaximum(), baselineMetrics.getMetrics().getMaximum());

        final double maxDelta = testMetrics.getMetrics().getMaximum() - baselineMetrics.getMetrics().getMaximum();
        LOG.info("Delta: {}", maxDelta);
        logDeltas(maxDelta, "maximum rate");

        LOG.info("");
        LOG.info("MEAN (RATE):");
        LOG.info("Test: {} | Baseline: {}", testMetrics.getMetrics().getMean(), baselineMetrics.getMetrics().getMean());

        final double meanDelta = testMetrics.getMetrics().getMean() - baselineMetrics.getMetrics().getMean();
        LOG.info("Delta: {}", meanDelta);
        logDeltas(meanDelta, "mean rate");

        LOG.info("");
        LOG.info("GEOMETRIC MEAN (RATE):");
        LOG.info("Test: {} | Baseline: {}", testMetrics.getMetrics().getGeoMean(), baselineMetrics.getMetrics().getGeoMean());

        final double geoMeanDelta = testMetrics.getMetrics().getGeoMean() - baselineMetrics.getMetrics().getGeoMean();
        LOG.info("Delta: {}", geoMeanDelta);
        logDeltas(geoMeanDelta, "geometric mean rate");

        LOG.info("");
        LOG.info("OTHER:");
        LOG.info("Test standard deviation: {} | Baseline standard deviation: {}", testMetrics.getMetrics().getStdDeviation(),
                baselineMetrics.getMetrics().getStdDeviation());
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

    private void doLogPrintLatency(Supplier<Double> testInfoSuplier, Supplier<Double> baselineInfoSuppler, String title) {
        LOG.info("{} latency: {} / baseline: {}", title, testInfoSuplier.get(), baselineInfoSuppler.get());

        double p50Delta = testInfoSuplier.get() - baselineInfoSuppler.get();
        logDeltas(p50Delta, title);
    }

    @Override
    public void outputHistogram(BaselinedTestMetrics testMetrics) {
        LOG.info("LATENCY INFO:");
        LOG.info("Latency start time: {}", testMetrics.getTestMetrics().getMetrics().getStartTimeStamp());
        LOG.info("Latency end time: {}", testMetrics.getTestMetrics().getMetrics().getEndTimeStamp());
        LOG.info("Latency max value: {}", testMetrics.getTestMetrics().getMetrics().getMaxLatency());

        doLogPrintLatency(testMetrics.getTestMetrics().getMetrics()::getP50Latency,
                testMetrics.getBaselineMetrics().getMetrics()::getP50Latency, "p50 (median)");

        doLogPrintLatency(testMetrics.getTestMetrics().getMetrics()::getP90Latency,
                testMetrics.getBaselineMetrics().getMetrics()::getP90Latency, "p90");

        doLogPrintLatency(testMetrics.getTestMetrics().getMetrics()::getP95Latency,
                testMetrics.getBaselineMetrics().getMetrics()::getP95Latency, "p95");

        doLogPrintLatency(testMetrics.getTestMetrics().getMetrics()::getP99Latency,
                testMetrics.getBaselineMetrics().getMetrics()::getP99Latency, "p99");

        doLogPrintLatency(testMetrics.getTestMetrics().getMetrics()::getP999Latency,
                testMetrics.getBaselineMetrics().getMetrics()::getP999Latency, "p99.9");
    }
}
