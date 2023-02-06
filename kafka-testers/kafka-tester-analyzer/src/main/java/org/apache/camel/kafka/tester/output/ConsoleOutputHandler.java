package org.apache.camel.kafka.tester.output;

import java.util.function.Supplier;

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
    public void output(TestMetrics testMetrics) {
        LOG.info("Test suite version: {}", testMetrics.getTestSuiteVersion());
        LOG.info("Type: {}", testMetrics.getType());
        LOG.info("Total: {}", testMetrics.getMetrics().getTotal());
        LOG.info("Minimum: {}", testMetrics.getMetrics().getMinimum());
        LOG.info("Maximum: {}", testMetrics.getMetrics().getMaximum());
        LOG.info("Mean: {}", testMetrics.getMetrics().getMean());
        LOG.info("Geometric mean: {}", testMetrics.getMetrics().getGeoMean());
        LOG.info("Standard deviation: {}", testMetrics.getMetrics().getStdDeviation());

        LOG.info("Latency start time: {}", testMetrics.getMetrics().getStartTimeStamp());
        LOG.info("Latency end time: {}", testMetrics.getMetrics().getEndTimeStamp());
        LOG.info("Latency max value: {}", testMetrics.getMetrics().getMaxLatency());
        LOG.info("p50 (median) latency: {}", testMetrics.getMetrics().getP50Latency());
        LOG.info("p90 latency: {}", testMetrics.getMetrics().getP90Latency());
        LOG.info("p95 latency: {}", testMetrics.getMetrics().getP95Latency());
        LOG.info("p99 latency: {}", testMetrics.getMetrics().getP99Latency());
        LOG.info("p99.9 latency: {}", testMetrics.getMetrics().getP999Latency());
    }


    @Override
    public void output(BaselinedTestMetrics baselinedTestMetrics) {
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

        LOG.info("LATENCY INFO:");
        LOG.info("Latency start time: {}", testMetrics.getMetrics().getStartTimeStamp());
        LOG.info("Latency end time: {}", testMetrics.getMetrics().getEndTimeStamp());
        LOG.info("Latency max value: {}", testMetrics.getMetrics().getMaxLatency());

        doLogPrintLatency(testMetrics.getMetrics()::getP50Latency,
                baselineMetrics.getMetrics()::getP50Latency, "p50 (median)");

        doLogPrintLatency(testMetrics.getMetrics()::getP90Latency,
                baselineMetrics.getMetrics()::getP90Latency, "p90");

        doLogPrintLatency(testMetrics.getMetrics()::getP95Latency,
                baselineMetrics.getMetrics()::getP95Latency, "p95");

        doLogPrintLatency(testMetrics.getMetrics()::getP99Latency,
                baselineMetrics.getMetrics()::getP99Latency, "p99");

        doLogPrintLatency(testMetrics.getMetrics()::getP999Latency,
                baselineMetrics.getMetrics()::getP999Latency, "p99.9");
    }

    private void doLogPrintLatency(Supplier<Double> testInfoSuplier, Supplier<Double> baselineInfoSuppler, String title) {
        LOG.info("{} latency: {} / baseline: {}", title, testInfoSuplier.get(), baselineInfoSuppler.get());

        double p50Delta = testInfoSuplier.get() - baselineInfoSuppler.get();
        logDeltas(p50Delta, title);
    }
}
