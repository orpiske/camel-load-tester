package org.apache.camel.kafka.tester;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.HdrHistogram.Histogram;
import org.apache.camel.kafka.tester.common.types.BaselinedTestMetrics;
import org.apache.camel.kafka.tester.output.ConsoleOutputHandler;
import org.apache.camel.kafka.tester.output.DelegateOutputHandler;
import org.apache.camel.kafka.tester.output.JsonOutputHandler;
import org.apache.camel.kafka.tester.output.PropertiesOutputHandler;
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

        BaselinedTestMetrics baselinedTestMetrics = null;
        final Plotter plotter = new Plotter(OUTPUT_DIR);

        final DelegateOutputHandler delegateOutputHandler = new DelegateOutputHandler();



        delegateOutputHandler.add(new ConsoleOutputHandler());
        final PropertiesOutputHandler propertiesOutputHandler = new PropertiesOutputHandler();
        delegateOutputHandler.add(propertiesOutputHandler);

        delegateOutputHandler.add(new JsonOutputHandler(OUTPUT_DIR));

        String baselineRateFile = System.getProperty("baseline.rate.file");
        if (baselineRateFile != null) {
            RateData baselineRateData = analyzerApp.getRateData(baselineRateFile);

             baselinedTestMetrics = analyzerApp.analyze(testRateData, baselineRateData, delegateOutputHandler);
            plotter.plot(testRateData, baselineRateData);
        } else {
            analyzerApp.analyze(testRateData, delegateOutputHandler);
            plotter.plot(testRateData);
        }

        Optional<Histogram> testHistogram = analyzerApp.getAccumulated(testLatencies);
        if (testHistogram.isPresent()) {
            String baselineLatencies = System.getProperty("baseline.latencies.file");
            Optional<Histogram> baseLineHistogram = analyzerApp.getAccumulated(baselineLatencies);

            if (baseLineHistogram.isPresent()) {
                analyzerApp.analyze(testHistogram.get(), baseLineHistogram.get(), baselinedTestMetrics, delegateOutputHandler);
                analyzerApp.plot(testHistogram.get(), baseLineHistogram.get(), delegateOutputHandler);
            } else {
                analyzerApp.analyze(testHistogram.get(), delegateOutputHandler);
                analyzerApp.plot(testHistogram.get());
            }
        }

        ReportGenerator reportGenerator = new ReportGenerator(propertiesOutputHandler.getProperties(), OUTPUT_DIR);

        if (baselineRateFile != null) {
            try {
                reportGenerator.generate();
            } catch (Exception e) {
                LOG.error(e.getMessage());
                System.exit(1);
            }
        } else {
            LOG.warn("Skipping generating report because there's no baseline");
        }
    }


}
