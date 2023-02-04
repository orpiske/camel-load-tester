package org.apache.camel.kafka.tester;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.HdrHistogram.DoubleHistogram;
import org.HdrHistogram.EncodableHistogram;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramLogReader;
import org.apache.camel.CamelException;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.kafka.tester.io.BinaryRateReader;
import org.apache.camel.kafka.tester.io.common.FileHeader;
import org.apache.camel.kafka.tester.io.common.RateEntry;
import org.apache.camel.kafka.tester.output.OutputHandler;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyzerApp {
    private static final Logger LOG = LoggerFactory.getLogger(AnalyzerApp.class);
    private static final String TEST_NAME = System.getProperty("test.name");
    private static final String TIME_UNIT_NAME = System.getProperty("latencies.timeunit", "microseconds");
    private static final String OUTPUT_DIR = System.getProperty("output.dir", ".");

    private final Properties properties = new Properties();
    
    public void plot(Histogram histogram) throws IOException {
        HdrPlotter hdrPlotter = new HdrPlotter(OUTPUT_DIR, "latency", TIME_UNIT_NAME);

        hdrPlotter.plot(histogram);
    }

    public void plot(Histogram testHistogram, Histogram baseline) throws IOException {
        HdrPlotter hdrPlotter = new HdrPlotter(OUTPUT_DIR,"latency", TIME_UNIT_NAME);

        AbstractHdrPlotter.SeriesData seriesData = new AbstractHdrPlotter.SeriesData();

        seriesData.seriesName = "Baseline " + (baseline.getTag() == null ? "" : baseline.getTag());
        seriesData.yData = baseline;

        hdrPlotter.plot(testHistogram, seriesData);
        properties.put("latencyFile", hdrPlotter.getFileName());
    }

    public void analyze(Histogram histogram, OutputHandler outputHandler) {
        outputHandler.outputHistogram(histogram);
    }

    public void analyze(Histogram histogram, Histogram baseline, OutputHandler outputHandler) {
        outputHandler.outputHistogram(histogram, baseline);

        double p50Delta = histogram.getValueAtPercentile(50.0) - baseline.getValueAtPercentile(50.0);
        properties.put("testP50", histogram.getValueAtPercentile(50.0));
        properties.put("baselineP50", baseline.getValueAtPercentile(50.0));
        properties.put("deltaP50", p50Delta);

        double p90Delta = histogram.getValueAtPercentile(90.0) - baseline.getValueAtPercentile(90.0);
        properties.put("testP90", histogram.getValueAtPercentile(90.0));
        properties.put("baselineP90", baseline.getValueAtPercentile(90.0));
        properties.put("deltaP90", p90Delta);

        double p95Delta = histogram.getValueAtPercentile(95.0) - baseline.getValueAtPercentile(95.0);
        properties.put("testP95", histogram.getValueAtPercentile(95.0));
        properties.put("baselineP95", baseline.getValueAtPercentile(95.0));
        properties.put("deltaP95", p95Delta);

        double p99Delta = histogram.getValueAtPercentile(99.0) - baseline.getValueAtPercentile(99.0);
        properties.put("testP99", histogram.getValueAtPercentile(99.0));
        properties.put("baselineP99", baseline.getValueAtPercentile(99.0));
        properties.put("deltaP99", p99Delta);

        double p999Delta = histogram.getValueAtPercentile(99.9) - baseline.getValueAtPercentile(99.9);
        properties.put("testP999", histogram.getValueAtPercentile(99.9));
        properties.put("baselineP999", baseline.getValueAtPercentile(99.9));
        properties.put("deltaP999", p999Delta);
    }

    public void plot(RateData testData) throws IOException {
        RatePlotter plotter = createStandardPlotter(testData.header);

        List<Date> xData = testData.entries.stream().map(r -> new Date(r.getTimestamp())).collect(Collectors.toList());
        List<Long> yData = testData.entries.stream().map(RateEntry::getCount).collect(Collectors.toList());
        plotter.plot(xData, yData);
    }

    public void plot(RateData testData, RateData baselineData) throws IOException {
        RatePlotter plotter = createStandardPlotter(testData.header);

        List<Date> xData = testData.entries.stream().map(r -> new Date(r.getTimestamp())).collect(Collectors.toList());
        List<Long> yDataTest = testData.entries.stream().map(RateEntry::getCount).collect(Collectors.toList());
        List<Long> yDataBaseline = baselineData.entries.stream().map(RateEntry::getCount).collect(Collectors.toList());

        AbstractRatePlotter.SeriesData seriesData = new AbstractRatePlotter.SeriesData();

        seriesData.seriesName = "Baseline " + baselineData.header.getCamelVersion();
        seriesData.yData = yDataBaseline;
        plotter.plot(xData, yDataTest, seriesData);
        properties.put("rateFile", plotter.getFileName());
    }

    private RatePlotter createStandardPlotter(FileHeader testData) {
        RatePlotter plotter = new RatePlotter(OUTPUT_DIR, testData.getRole().toString().toLowerCase(Locale.ROOT));
        ChartProperties chartProperties = new ChartProperties();

        chartProperties.setSeriesName(ChartProperties.capitilizeOnly(testData.getRole().toString()) + " " + testData.getCamelVersion());
        plotter.setChartProperties(chartProperties);
        return plotter;
    }

    public void analyze(RateData testData, OutputHandler outputHandler) {
        SummaryStatistics testStatistics = getStats(testData);

        outputHandler.outputSingleAnalyzis(testData, testStatistics);
    }

    public void analyze(RateData testData, RateData baselineData, OutputHandler outputHandler) {
        SummaryStatistics testStatistics = getStats(testData);
        SummaryStatistics baselineStatistics = getStats(baselineData);

        outputHandler.outputWithBaseline(testData, testStatistics, baselineData, baselineStatistics);

        properties.put("testCamelVersion", testData.header.getCamelVersion());
        properties.put("baselineCamelVersion", baselineData.header.getCamelVersion());
        properties.put("testTotalExchanges", testStatistics.getSum());
        properties.put("baselineTotalExchanges", baselineStatistics.getSum());

        double totalDelta = testStatistics.getSum() - baselineStatistics.getSum();
        properties.put("deltaTotalExchanges", totalDelta);

        final double minDelta = testStatistics.getMin() - baselineStatistics.getMin();
        properties.put("testRateMin", testStatistics.getMin());
        properties.put("baselineRateMin", baselineStatistics.getMin());
        properties.put("deltaRateMin", minDelta);

        final double maxDelta = testStatistics.getMax() - baselineStatistics.getMax();
        properties.put("testRateMax", testStatistics.getMax());
        properties.put("baselineRateMax", baselineStatistics.getMax());
        properties.put("deltaRateMax", maxDelta);

        final double meanDelta = testStatistics.getMean() - baselineStatistics.getMean();
        properties.put("testRateMean", testStatistics.getMean());
        properties.put("baselineRateMean", baselineStatistics.getMean());
        properties.put("deltaRateMean", meanDelta);

        final double geoMeanDelta = testStatistics.getGeometricMean() - baselineStatistics.getGeometricMean();
        properties.put("testRateGeoMean", testStatistics.getGeometricMean());
        properties.put("baselineRateGeoMean", baselineStatistics.getGeometricMean());
        properties.put("deltaRateGeoMean", geoMeanDelta);

        properties.put("testStdDev", testStatistics.getStandardDeviation());
        properties.put("baselineStdDev", baselineStatistics.getStandardDeviation());
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

    public void generateReport() throws IOException, CamelException {
        VelocityTemplateParser templateParser = new VelocityTemplateParser(properties);

        File outputFile;
        properties.put("testName", TEST_NAME);
        outputFile = templateParser.getOutputFile(new File(OUTPUT_DIR));

        try (FileWriter fw = new FileWriter(outputFile)) {
            templateParser.parse(fw);
            LOG.info("Template file was written to {}", outputFile);
        }
    }
}
