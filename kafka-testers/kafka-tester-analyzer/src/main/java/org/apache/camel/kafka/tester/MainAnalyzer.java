package org.apache.camel.kafka.tester;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainAnalyzer {
    private static final Logger LOG = LoggerFactory.getLogger(MainAnalyzer.class);
    private static final String OUTPUT_DIR = System.getProperty("output.dir", ".");

    public static void main(String[] args) throws IOException {
        String testRateFile = System.getProperty("test.rate.file");
        String testLatencies = System.getProperty("test.latencies.file");

        Files.createDirectories(Path.of(OUTPUT_DIR));

        AnalyzerApp analyzerApp = new AnalyzerApp();

        RateData testRateData = analyzerApp.getRateData(testRateFile);

        String baselineRateFile = System.getProperty("baseline.rate.file");
        if (baselineRateFile != null) {
            RateData baselineRateData = analyzerApp.getRateData(baselineRateFile);

            analyzerApp.analyze(testRateData, baselineRateData);
            analyzerApp.plot(testRateData, baselineRateData);
        } else {
            analyzerApp.analyze(testRateData);
            analyzerApp.plot(testRateData);
        }

        Optional<Histogram> testHistogram = analyzerApp.getAccumulated(testLatencies);
        if (testHistogram.isPresent()) {
            String baselineLatencies = System.getProperty("baseline.latencies.file");
            Optional<Histogram> baseLineHistogram = analyzerApp.getAccumulated(baselineLatencies);

            if (baseLineHistogram.isPresent()) {
                analyzerApp.analyze(testHistogram.get(), baseLineHistogram.get());
                analyzerApp.plot(testHistogram.get(), baseLineHistogram.get());
            } else {
                analyzerApp.analyze(testHistogram.get());
                analyzerApp.plot(testHistogram.get());
            }
        }

        if (baselineRateFile != null) {
            try {
                analyzerApp.generateReport();
            } catch (Exception e) {
                LOG.error(e.getMessage());
                System.exit(1);
            }
        } else {
            LOG.warn("Skipping generating report because there's no baseline");
        }
    }


}
