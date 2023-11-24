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

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

import org.apache.camel.load.tester.io.RateWriter;
import org.apache.camel.load.tester.io.RecordState;

public class WriterReporter extends AbstractReporter {

    private final RateWriter writer;
    private Consumer<Long> onComplete;

    public WriterReporter(RateWriter writer, long testSize, Reporter.Action staleAction, Consumer<Long> onComplete) {
        super(testSize, staleAction, Counter.getInstance().getAdder());
        this.writer = writer;
        this.onComplete = onComplete;
    }

    protected void update(LongAdder longAdder) {
        long difference = measure(longAdder);

        try {
            final Instant now = Instant.now();
            RecordState state = writer.write(0, difference, now);
            if (state == RecordState.OUTDATED) {
                LOG.error("Sequential record with a timestamp in the past: {}", now);
            } else if (state == RecordState.DUPLICATED) {
                LOG.error("Multiple records for within the same second slot: {}", now);
            }
        } catch (IOException e) {
            LOG.error("Unable to update records: {}", e.getMessage());
        }
    }

    @Override
    public void closeReport() {
        if (!skipExceededCount()) {
            long difference = measure(longAdder);

            try {
                writer.tryWrite(0, difference, Instant.now());
                LOG.info("Processed {} messages", lastCount);
            } catch (IOException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.error("Unable to perform the last update: {}", e.getMessage(), e);
                } else {
                    LOG.error("Unable to perform the last update: {}", e.getMessage());
                }
            } finally {
                if (onComplete != null) {
                    onComplete.accept(lastCount);
                }
            }
        }
    }

}
