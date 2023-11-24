/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.load.analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.HdrHistogram.Histogram;
import org.apache.camel.load.tester.common.types.BaselinedTestMetrics;
import org.apache.camel.load.tester.common.types.TestMetrics;
import org.apache.camel.load.analyzer.output.ConsoleOutputHandler;
import org.apache.camel.load.analyzer.output.DelegateOutputHandler;
import org.apache.camel.load.analyzer.output.JsonOutputHandler;
import org.apache.camel.load.analyzer.output.NoLatencyConsoleOutputHandler;
import org.apache.camel.load.analyzer.output.PropertiesOutputHandler;
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

        TestMetrics testMetrics = null;
        BaselinedTestMetrics baselinedTestMetrics = null;
        final Plotter plotter = new Plotter(OUTPUT_DIR);

        final DelegateOutputHandler delegateOutputHandler = new DelegateOutputHandler();

        if (testLatencies == null) {
            delegateOutputHandler.add(new NoLatencyConsoleOutputHandler());
        } else {
            delegateOutputHandler.add(new ConsoleOutputHandler());
        }

        final PropertiesOutputHandler propertiesOutputHandler = new PropertiesOutputHandler();
        delegateOutputHandler.add(propertiesOutputHandler);

        delegateOutputHandler.add(new JsonOutputHandler(OUTPUT_DIR));

        String baselineRateFile = System.getProperty("baseline.rate.file");
        if (baselineRateFile != null) {
            RateData baselineRateData = analyzerApp.getRateData(baselineRateFile);

             baselinedTestMetrics = analyzerApp.analyze(testRateData, baselineRateData, delegateOutputHandler);
            plotter.plot(testRateData, baselineRateData);
        } else {
            testMetrics = analyzerApp.analyze(testRateData, delegateOutputHandler);
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
                analyzerApp.analyze(testHistogram.get(), testMetrics, delegateOutputHandler);
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
