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

package org.apache.camel.load.tester.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.HdrHistogram.EncodableHistogram;
import org.HdrHistogram.HistogramLogWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes the latency data in the HdrHistogram data format.
 *
 * @see <a href="https://github.com/HdrHistogram/HdrHistogram">HdrHistogram</a>
 */
public final class LatencyWriter implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(LatencyWriter.class);

    private final HistogramLogWriter logWriter;
    private final OutputStream out;

    /**
     * Constructor
     *
     * @param path file path
     * @throws IOException on I/O errors
     */
    public LatencyWriter(final File path) throws IOException {
        out = new FileOutputStream(path);
        logWriter = new HistogramLogWriter(this.out);
    }

    /**
     * Writes the legend for the HDR file
     * @param startedEpochMillis the time the test started (in milliseconds from epoch)
     */
    public void outputLegend(long startedEpochMillis) {
        logWriter.outputComment("[mpt]");
        logWriter.outputLogFormatVersion();
        logWriter.outputStartTime(startedEpochMillis);
        logWriter.outputLegend();
    }

    /**
     * Sets up the output interval histogram
     * @param histogram the output interval histogram
     */
    public void outputIntervalHistogram(EncodableHistogram histogram) {
        logWriter.outputIntervalHistogram(histogram);
    }

    /**
     * Closes the writer
     */
    @Override
    public void close() {
        try {
            //to be sure everything has been correctly written (not necessary)
            this.out.flush();
        } catch (Exception e) {
            LOG.error("Unable to flush records: {}", e.getMessage());
        } finally {
            this.logWriter.close();
        }
    }
}
