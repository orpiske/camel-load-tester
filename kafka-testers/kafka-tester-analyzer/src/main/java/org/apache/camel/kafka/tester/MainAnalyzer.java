package org.apache.camel.kafka.tester;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.HdrHistogram.DoubleHistogram;
import org.HdrHistogram.EncodableHistogram;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramLogReader;
import org.apache.camel.kafka.tester.io.common.FileHeader;
import org.apache.camel.kafka.tester.io.common.RateEntry;
import org.apache.camel.kafka.tester.io.BinaryRateReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainAnalyzer {
    private static final Logger LOG = LoggerFactory.getLogger(MainAnalyzer.class);

    public static void main(String[] args) throws IOException {
        String testRateFile = System.getProperty("test.rate.file");
        String testLatencies = System.getProperty("test.latencies.file");

        try (BinaryRateReader rateReader = new BinaryRateReader(new File(testRateFile))) {
            FileHeader header = rateReader.getHeader();

            LOG.info("Analyzing: {}", header.getTestSuiteVersion());
            LOG.info("Version: {}", header.getFileVersion());
            LOG.info("Type: {}", header.getRole().name());

            List<RateEntry> entries = new ArrayList<>();

            RateEntry entry = rateReader.readRecord();
            while (entry != null) {
                entries.add(entry);
                entry = rateReader.readRecord();
            }

            RateEntry minimal = entries.stream().min(Comparator.comparing(e -> e.getCount())).get();
            RateEntry maximum = entries.stream().max(Comparator.comparing(e -> e.getCount())).get();
            long total = entries.stream().collect(Collectors.summingLong(r -> r.getCount()));

            double average = (double) total / (double) entries.size();

            LOG.info("Minimum: {} at {}", minimal.getCount(), Instant.ofEpochSecond(minimal.getTimestamp()));
            LOG.info("Maximum: {} at {}", maximum.getCount(), Instant.ofEpochSecond(maximum.getTimestamp()));
            LOG.info("Average: {}", average);
        }

        File latenciesFile = new File(testLatencies);

        // Only producer has those at the moment
        if (latenciesFile.exists()) {
            Histogram histogram = getAccumulated(latenciesFile);

            LOG.info("Latency start time: {}", histogram.getStartTimeStamp());
            LOG.info("Latency end time: {}", histogram.getEndTimeStamp());
            LOG.info("Latency max value: {}", histogram.getMaxValue());
            LOG.info("p50 (median) latency: {}", histogram.getValueAtPercentile(50.0));
            LOG.info("p90 latency: {}", histogram.getValueAtPercentile(90.0));
            LOG.info("p95 latency: {}", histogram.getValueAtPercentile(95.0));
            LOG.info("p99 latency: {}", histogram.getValueAtPercentile(99.0));
            LOG.info("p99.9 latency: {}", histogram.getValueAtPercentile(99.9));
        } else {
            LOG.warn("Latency file does not exist");
        }


    }


    public static Histogram getAccumulated(final File histogramFile) throws FileNotFoundException {
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
