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

package org.apache.camel.load.analyzer.output;

import java.util.Properties;
import java.util.function.Supplier;

import org.apache.camel.load.tester.common.types.BaselinedTestMetrics;
import org.apache.camel.load.tester.common.types.TestMetrics;

public class PropertiesOutputHandler implements OutputHandler {
    private static final String testName = System.getProperty("test.name");
    private Properties properties = new Properties();

    @Override
    public void output(TestMetrics testMetrics) {
        // NO-OP
    }

    @Override
    public void output(BaselinedTestMetrics testMetrics) {
        properties.put("testName", testName);

        properties.put("testCamelVersion", testMetrics.getTestMetrics().getSutVersion());
        properties.put("baselineCamelVersion", testMetrics.getBaselineMetrics().getSutVersion());
        properties.put("testTotalExchanges", testMetrics.getTestMetrics().getMetrics().getTotal());
        properties.put("baselineTotalExchanges", testMetrics.getBaselineMetrics().getMetrics().getTotal());

        double totalDelta = testMetrics.getTestMetrics().getMetrics().getTotal() - testMetrics.getBaselineMetrics().getMetrics().getTotal();
        properties.put("deltaTotalExchanges", totalDelta);

        final double minDelta = testMetrics.getTestMetrics().getMetrics().getMinimum() - testMetrics.getBaselineMetrics().getMetrics().getMinimum();
        properties.put("testRateMin", testMetrics.getTestMetrics().getMetrics().getMinimum());
        properties.put("baselineRateMin", testMetrics.getBaselineMetrics().getMetrics().getMinimum());
        properties.put("deltaRateMin", minDelta);

        final double maxDelta = testMetrics.getTestMetrics().getMetrics().getMaximum() - testMetrics.getBaselineMetrics().getMetrics().getMaximum();
        properties.put("testRateMax", testMetrics.getTestMetrics().getMetrics().getMaximum());
        properties.put("baselineRateMax", testMetrics.getBaselineMetrics().getMetrics().getMaximum());
        properties.put("deltaRateMax", maxDelta);

        final double meanDelta = testMetrics.getTestMetrics().getMetrics().getMean() - testMetrics.getBaselineMetrics().getMetrics().getMean();
        properties.put("testRateMean", testMetrics.getTestMetrics().getMetrics().getMean());
        properties.put("baselineRateMean", testMetrics.getBaselineMetrics().getMetrics().getMean());
        properties.put("deltaRateMean", meanDelta);

        final double geoMeanDelta = testMetrics.getTestMetrics().getMetrics().getGeoMean() - testMetrics.getBaselineMetrics().getMetrics().getGeoMean();
        properties.put("testRateGeoMean", testMetrics.getTestMetrics().getMetrics().getGeoMean());
        properties.put("baselineRateGeoMean", testMetrics.getBaselineMetrics().getMetrics().getGeoMean());
        properties.put("deltaRateGeoMean", geoMeanDelta);

        properties.put("testStdDev", testMetrics.getTestMetrics().getMetrics().getStdDeviation());
        properties.put("baselineStdDev", testMetrics.getBaselineMetrics().getMetrics().getStdDeviation());

        doPut(testMetrics.getTestMetrics().getMetrics()::getP50Latency, testMetrics.getBaselineMetrics().getMetrics()::getP50Latency,
                "P50");
        doPut(testMetrics.getTestMetrics().getMetrics()::getP90Latency, testMetrics.getBaselineMetrics().getMetrics()::getP90Latency,
                "P90");
        doPut(testMetrics.getTestMetrics().getMetrics()::getP95Latency, testMetrics.getBaselineMetrics().getMetrics()::getP95Latency,
                "P95");
        doPut(testMetrics.getTestMetrics().getMetrics()::getP99Latency, testMetrics.getBaselineMetrics().getMetrics()::getP99Latency,
                "P99");
        doPut(testMetrics.getTestMetrics().getMetrics()::getP999Latency, testMetrics.getBaselineMetrics().getMetrics()::getP999Latency,
                "P999");

        properties.put("latencyFile", "latency_all.png");

    }

    private void doPut(Supplier<Double> testInfoSuplier, Supplier<Double> baselineInfoSuppler, String title) {
        double delta = testInfoSuplier.get() - baselineInfoSuppler.get();
        properties.put(String.format("test%s", title), testInfoSuplier.get());
        properties.put(String.format("baseline%s", title), baselineInfoSuppler.get());
        properties.put(String.format("delta%s", title), delta);
    }

    public Properties getProperties() {
        return properties;
    }
}
