package org.apache.camel.kafka.tester;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.HdrHistogram.DoubleHistogram;
import org.HdrHistogram.EncodableHistogram;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramLogReader;
import org.apache.camel.kafka.tester.io.BinaryRateReader;
import org.apache.camel.kafka.tester.io.common.FileHeader;
import org.apache.camel.kafka.tester.io.common.RateEntry;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainAnalyzer {
    private static final Logger LOG = LoggerFactory.getLogger(MainAnalyzer.class);
    private static final String TIME_UNIT_NAME = System.getProperty("latencies.timeunit", "microseconds");

    private static class RateData {
        FileHeader header;
        List<RateEntry> entries;
    }

    public static void main(String[] args) throws IOException {
        String testRateFile = System.getProperty("test.rate.file");
        String testLatencies = System.getProperty("test.latencies.file");

        RateData testRateData = getRateData(testRateFile);

        String baselineRateFile = System.getProperty("baseline.rate.file");
        if (baselineRateFile != null) {
            RateData baselineRateData = getRateData(baselineRateFile);

            analyze(testRateData, baselineRateData);
            plot(testRateData, baselineRateData);
        } else {
            analyze(testRateData);
            plot(testRateData);
        }

        Optional<Histogram> testHistogram = getAccumulated(testLatencies);
        if (testHistogram.isPresent()) {
            String baselineLatencies = System.getProperty("baseline.latencies.file");
            Optional<Histogram> baseLineHistogram = getAccumulated(baselineLatencies);

            if (baseLineHistogram.isPresent()) {
                analyze(testHistogram.get(), baseLineHistogram.get());
                plot(testHistogram.get(), baseLineHistogram.get());
            } else {
                analyze(testHistogram.get());
                plot(testHistogram.get());
            }
        } else {
            LOG.warn("Latency file does not exist");
        }
    }

    private static void plot(Histogram histogram) throws IOException {
        HdrPlotter hdrPlotter = new HdrPlotter("latency", TIME_UNIT_NAME);

        hdrPlotter.plot(histogram);
    }

    private static void plot(Histogram testHistogram, Histogram baseline) throws IOException {
        HdrPlotter hdrPlotter = new HdrPlotter("latency", TIME_UNIT_NAME);

        AbstractHdrPlotter.SeriesData seriesData = new AbstractHdrPlotter.SeriesData();

        seriesData.seriesName = "Baseline " + (baseline.getTag() == null ? "" : baseline.getTag());
        seriesData.yData = baseline;

        hdrPlotter.plot(testHistogram, seriesData);
    }

    private static void analyze(Histogram histogram) {
        LOG.info("Latency start time: {}", histogram.getStartTimeStamp());
        LOG.info("Latency end time: {}", histogram.getEndTimeStamp());
        LOG.info("Latency max value: {}", histogram.getMaxValue());
        LOG.info("p50 (median) latency: {}", histogram.getValueAtPercentile(50.0));
        LOG.info("p90 latency: {}", histogram.getValueAtPercentile(90.0));
        LOG.info("p95 latency: {}", histogram.getValueAtPercentile(95.0));
        LOG.info("p99 latency: {}", histogram.getValueAtPercentile(99.0));
        LOG.info("p99.9 latency: {}", histogram.getValueAtPercentile(99.9));
    }


    private static void analyze(Histogram histogram, Histogram baseline) {
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

    private static void plot(RateData testData) throws IOException {
        RatePlotter plotter = createStandardPlotter(testData.header);

        List<Date> xData = testData.entries.stream().map(r -> new Date(r.getTimestamp())).collect(Collectors.toList());
        List<Long> yData = testData.entries.stream().map(RateEntry::getCount).collect(Collectors.toList());
        plotter.plot(xData, yData);
    }

    private static void plot(RateData testData, RateData baselineData) throws IOException {
        RatePlotter plotter = createStandardPlotter(testData.header);

        List<Date> xData = testData.entries.stream().map(r -> new Date(r.getTimestamp())).collect(Collectors.toList());
        List<Long> yDataTest = testData.entries.stream().map(RateEntry::getCount).collect(Collectors.toList());
        List<Long> yDataBaseline = baselineData.entries.stream().map(RateEntry::getCount).collect(Collectors.toList());

        AbstractRatePlotter.SeriesData seriesData = new AbstractRatePlotter.SeriesData();

        seriesData.seriesName = "Baseline " + baselineData.header.getCamelVersion();
        seriesData.yData = yDataBaseline;
        plotter.plot(xData, yDataTest, seriesData);
    }

    private static RatePlotter createStandardPlotter(FileHeader testData) {
        RatePlotter plotter = new RatePlotter(testData.getRole().toString().toLowerCase(Locale.ROOT));
        ChartProperties chartProperties = new ChartProperties();

        chartProperties.setSeriesName(ChartProperties.capitilizeOnly(testData.getRole().toString()) + " " + testData.getCamelVersion());
        plotter.setChartProperties(chartProperties);
        return plotter;
    }

    private static void analyze(RateData testData) {
        SummaryStatistics testStatistics = getStats(testData);

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

    private static void analyze(RateData testData, RateData baselineData) {
        SummaryStatistics testStatistics = getStats(testData);
        SummaryStatistics baselineStatistics = getStats(baselineData);

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

        LOG.info("");
        LOG.info("SUMMARY:");
    }

    private static void logDeltas(double delta, String name) {
        if (delta < 0) {
            LOG.warn("The {} was lower for the test than for the baseline", name);
        } else {
            LOG.info("The {} was greater for the test than for the baseline", name);
        }
    }


    private static SummaryStatistics getStats(RateData testData) {
        SummaryStatistics summaryStatistics = new SummaryStatistics();

        testData.entries.subList(10, testData.entries.size()).forEach(r -> summaryStatistics.addValue(r.getCount()));

        return summaryStatistics;
    }

    private static RateData getRateData(String filename) throws IOException {
        RateData rateData = new RateData();

        try (BinaryRateReader rateReader = new BinaryRateReader(new File(filename))) {
            rateData.header = rateReader.getHeader();

            rateData.entries = new ArrayList<>();

            RateEntry entry = rateReader.readRecord();
            while (entry != null) {
                rateData.entries.add(entry);
                entry = rateReader.readRecord();
            }
        }

        return rateData;
    }

    public static Optional<Histogram> getAccumulated(final String histogramFileName) throws FileNotFoundException {
        File histogramFile = new File(histogramFileName);

        return Optional.of(getAccumulated(histogramFile));
    }

    public static Histogram getAccumulated(final File histogramFile) throws FileNotFoundException {
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
