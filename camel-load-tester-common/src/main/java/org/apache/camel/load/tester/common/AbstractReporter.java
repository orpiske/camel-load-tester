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

import java.util.concurrent.atomic.LongAdder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractReporter implements Reporter {
    protected static final Logger LOG = LoggerFactory.getLogger(WriterReporter.class);
    protected final long testSize;
    protected final Action staleAction;
    protected final LongAdder longAdder;
    protected long lastCount = 0;
    // this one is updated every second (used for stale calculation)
    protected long lastReportedCount = 0;
    // This one stores the last reported count for rate calculation. It updates at a different interval than lastReportedCount
    protected long lastReportedCountRate = 0;

    private long staleCounter = 0;

    public AbstractReporter(long testSize, Action staleAction, LongAdder longAdder) {
        this.testSize = testSize;
        this.staleAction = staleAction;
        this.longAdder = longAdder;
    }

    abstract protected void update(LongAdder longAdder);

    protected long measure(LongAdder longAdder) {
        long currentCount = longAdder.longValue();
        long difference = currentCount - lastCount;
        lastCount = currentCount;

        return difference;
    }

    protected boolean skipExceededCount() {
        if (lastCount >= testSize) {
            LOG.warn("Processed more messages than setup: {} > {}", lastCount, testSize);

            return true;
        }

        return false;
    }

    private void progress(long testSize) {
        long delta = (testSize - lastCount);
        double progress = 100.0 * ((double) delta / (double) testSize);
        String message = String.format("Remaining: %.5f%% (%d of %d) - %d messages to finish", progress, lastCount, testSize, delta);

        LOG.info(message);


        double rate = (lastCount - lastReportedCountRate) / 10.0;
        String rateMessage = String.format("Current rate: %f exchanges/sec", rate);
        LOG.info(rateMessage);
        lastReportedCountRate = lastCount;
    }

    @Override
    public void staledCheck() {
        if (lastReportedCount == lastCount) {
            staleCounter++;
        } else {
            staleCounter = 0;
        }

        progress(testSize);

        if (staleCounter > 6) {
            LOG.warn("The test is stalled for too long");
            staleAction.execute();
        }
    }

    @Override
    public void update() {
        if (skipExceededCount()) {
            return;
        }

        lastReportedCount = lastCount;
        update(longAdder);
    }
}
