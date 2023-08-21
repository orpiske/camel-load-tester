package org.apache.camel.load.analyzer.output;

import org.apache.camel.load.tester.common.types.BaselinedTestMetrics;
import org.apache.camel.load.tester.common.types.TestMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoLatencyConsoleOutputHandler implements OutputHandler {
    private static final Logger LOG = LoggerFactory.getLogger(NoLatencyConsoleOutputHandler.class);

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
    }
}
