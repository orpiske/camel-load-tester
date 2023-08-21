package org.apache.camel.load.analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import org.HdrHistogram.DoubleHistogram;
import org.HdrHistogram.EncodableHistogram;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramLogReader;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.load.tester.common.types.BaselinedTestMetrics;
import org.apache.camel.load.tester.common.types.Metrics;
import org.apache.camel.load.tester.common.types.TestMetrics;
import org.apache.camel.load.tester.io.BinaryRateReader;
import org.apache.camel.load.tester.io.common.RateEntry;
import org.apache.camel.load.analyzer.output.OutputHandler;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyzerApp {
    private static final Logger LOG = LoggerFactory.getLogger(AnalyzerApp.class);
    private static final String TIME_UNIT_NAME = System.getProperty("latencies.timeunit", "microseconds");
    private static final String OUTPUT_DIR = System.getProperty("output.dir", ".");

    public void plot(Histogram histogram) throws IOException {
        HdrPlotter hdrPlotter = new HdrPlotter(OUTPUT_DIR, "latency", TIME_UNIT_NAME);

        hdrPlotter.plot(histogram);
    }

    public void plot(Histogram testHistogram, Histogram baseline, OutputHandler outputHandler) throws IOException {
        HdrPlotter hdrPlotter = new HdrPlotter(OUTPUT_DIR,"latency", TIME_UNIT_NAME);

        AbstractHdrPlotter.SeriesData seriesData = new AbstractHdrPlotter.SeriesData();

        seriesData.seriesName = "Baseline " + (baseline.getTag() == null ? "" : baseline.getTag());
        seriesData.yData = baseline;

        hdrPlotter.plot(testHistogram, seriesData);
    }

    public void analyze(Histogram histogram, TestMetrics testMetrics, OutputHandler outputHandler) {
        setLatencyMetrics(histogram, testMetrics);

        outputHandler.output(testMetrics);
    }

    public void analyze(Histogram histogram, Histogram baseline, BaselinedTestMetrics baselinedTestMetrics,OutputHandler outputHandler) {
        final TestMetrics testMetrics = baselinedTestMetrics.getTestMetrics();
        setLatencyMetrics(histogram, testMetrics);

        final TestMetrics baselineMetrics = baselinedTestMetrics.getBaselineMetrics();
        setLatencyMetrics(baseline, baselineMetrics);

        outputHandler.output(baselinedTestMetrics);
    }

    private static void setLatencyMetrics(Histogram histogram, TestMetrics testMetrics) {
        testMetrics.getMetrics().setStartTimeStamp(histogram.getStartTimeStamp());
        testMetrics.getMetrics().setEndTimeStamp(histogram.getEndTimeStamp());
        testMetrics.getMetrics().setMaxLatency(histogram.getMaxValue());

        testMetrics.getMetrics().setP50Latency(histogram.getValueAtPercentile(50.0));
        testMetrics.getMetrics().setP90Latency(histogram.getValueAtPercentile(90.0));
        testMetrics.getMetrics().setP99Latency(histogram.getValueAtPercentile(95.0));
        testMetrics.getMetrics().setP999Latency(histogram.getValueAtPercentile(99.0));
        testMetrics.getMetrics().setP50Latency(histogram.getValueAtPercentile(99.9));
    }

    public TestMetrics analyze(RateData testData, OutputHandler outputHandler) {
        SummaryStatistics testStatistics = getStats(testData);

        TestMetrics testMetrics = buildTestMetrics(testData, testStatistics);

        outputHandler.output(testMetrics);

        return testMetrics;
    }

    private static TestMetrics buildTestMetrics(RateData testData, SummaryStatistics testStatistics) {
        TestMetrics testMetrics = new TestMetrics();

        testMetrics.setTestSuiteVersion(testData.header.getCamelVersion());
        testMetrics.setSutVersion(testData.header.getCamelVersion());
        testMetrics.setType(testData.header.getRole().name());

        Metrics metrics = new Metrics();
        metrics.setTotal(testStatistics.getSum());
        metrics.setMinimum(testStatistics.getMin());
        metrics.setMaximum(testStatistics.getMax());
        metrics.setMean(testStatistics.getMean());
        metrics.setGeoMean(testStatistics.getGeometricMean());
        metrics.setStdDeviation(testStatistics.getStandardDeviation());

        testMetrics.setMetrics(metrics);
        return testMetrics;
    }

    public BaselinedTestMetrics analyze(RateData testData, RateData baselineData, OutputHandler outputHandler) {
        SummaryStatistics testStatistics = getStats(testData);
        SummaryStatistics baselineStatistics = getStats(baselineData);

        final BaselinedTestMetrics baselinedTestMetrics = new BaselinedTestMetrics();
        baselinedTestMetrics.setTestMetrics(buildTestMetrics(testData, testStatistics));
        baselinedTestMetrics.setBaselineMetrics(buildTestMetrics(baselineData, baselineStatistics));

        outputHandler.output(baselinedTestMetrics);

        return baselinedTestMetrics;
    }

    private SummaryStatistics getStats(RateData testData) {
        SummaryStatistics summaryStatistics = new SummaryStatistics();

        if (testData.entries.size() <= 10) {
            throw new RuntimeCamelException("There are not enough records to generate a summary. There are only " +
                    testData.entries.size());
        }

        testData.entries.subList(10, testData.entries.size()).forEach(r -> summaryStatistics.addValue(r.getCount()));

        return summaryStatistics;
    }

    public RateData getRateData(String filename) throws IOException {
        RateData rateData = new RateData();

        try (BinaryRateReader rateReader = new BinaryRateReader(new File(filename))) {
            rateData.header = rateReader.getHeader();

            rateData.entries = new ArrayList<>();

            RateEntry entry = rateReader.readRecord();
            while (entry != null) {
                LOG.trace("Reading: {}", entry);
                rateData.entries.add(entry);
                entry = rateReader.readRecord();
            }
        }

        return rateData;
    }

    public Optional<Histogram> getAccumulated(final String histogramFileName) throws FileNotFoundException {
        if (histogramFileName == null || histogramFileName.isEmpty()) {
            return Optional.ofNullable(null);
        }

        File histogramFile = new File(histogramFileName);

        return Optional.ofNullable(getAccumulated(histogramFile));
    }

    public Histogram getAccumulated(final File histogramFile) throws FileNotFoundException {
        if (!histogramFile.exists()) {
            return null;
        }

    Histogram accumulatedHistogram = null;
        DoubleHistogram accumulatedDoubleHistogram = null;

        HistogramLogReader histogramLogReader = new HistogramLogReader(histogramFile);

        int i = 0;
        while (histogramLogReader.hasNext()) {
            EncodableHistogram eh = histogramLogReader.nextIntervalHistogram();
            if (eh == null) {
                LOG.error("The histogram library returned an unexpected null value");
                break;
            }

            if (i == 0) {
                if (eh instanceof DoubleHistogram) {
                    accumulatedDoubleHistogram = ((DoubleHistogram) eh).copy();
                    accumulatedDoubleHistogram.reset();
                    accumulatedDoubleHistogram.setAutoResize(true);
                }
                else {
                    accumulatedHistogram = ((Histogram) eh).copy();
                    accumulatedHistogram.reset();
                    accumulatedHistogram.setAutoResize(true);
                }
            }

            LOG.trace("Processing histogram from point in time {} to {}",
                    Instant.ofEpochMilli(eh.getStartTimeStamp()), Instant.ofEpochMilli(eh.getEndTimeStamp()));

            if (eh instanceof DoubleHistogram) {
                Objects.requireNonNull(accumulatedDoubleHistogram).add((DoubleHistogram) eh);
            }
            else {
                Objects.requireNonNull(accumulatedHistogram).add((Histogram) eh);
            }

            i++;
        }

        if (accumulatedHistogram == null) {
            throw new AssertionError("The HDR data file did not contain any histogram data");
        }

        return accumulatedHistogram;
    }


}
