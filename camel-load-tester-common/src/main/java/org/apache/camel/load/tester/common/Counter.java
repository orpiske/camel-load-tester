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

package org.apache.camel.load.tester.common;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.HdrHistogram.EncodableHistogram;
import org.HdrHistogram.SingleWriterRecorder;
import org.apache.camel.Exchange;
import org.apache.camel.load.tester.io.LatencyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Counter {
    private static final Logger LOG = LoggerFactory.getLogger(Counter.class);

    private static Counter instance;
    private final LongAdder adder = new LongAdder();
    private final SingleWriterRecorder latencyRecorder =
            new SingleWriterRecorder(TimeUnit.HOURS.toMicros(1), 3);

    public static Counter getInstance() {
        if (instance == null) {
            instance = new Counter();
        }

        return instance;
    }

    public LongAdder getAdder() {
        return adder;
    }

    private void saveLatencyFile() {
        final String testLatenciesFileName = System.getProperty(Parameters.TEST_LATENCIES_FILE, "producer-latencies.hdr");
        final File path = IOUtil.create(testLatenciesFileName);

        try (LatencyWriter latencyWriter = new LatencyWriter(path)) {
            EncodableHistogram histogram = latencyRecorder.getIntervalHistogram();

            String camelVersion = System.getProperty(Parameters.CAMEL_VERSION, "3.x.x");
            histogram.setTag(camelVersion);
            latencyWriter.outputIntervalHistogram(histogram);
        } catch (IOException e) {
            System.err.println("Unable to save latency file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void setupLatencyRecorder() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveLatencyFile));
    }


    public void measureExchange(Exchange exchange) {
        Instant sent = exchange.getProperty("CREATE_TIME", Instant.class);
        if (sent != null) {
            Duration duration = Duration.between(sent, Instant.now());
            latencyRecorder.recordValue(TimeUnit.NANOSECONDS.toMicros(duration.toNanos()));
        } else {
            LOG.warn("Skipping latency processing for exchange due to missing CREATE_TIME property");
        }

    }
}
