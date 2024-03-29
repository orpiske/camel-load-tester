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

package org.apache.camel.load.tester.routes;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.load.tester.common.Parameters;
import org.apache.camel.load.tester.support.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ThreadedProducerTemplate extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadedProducerTemplate.class);

    private final int producerThreadCount;
    private final int testSize;
    private final ExecutorService executorService;
    private final int targetRate;
    private final File someFile = new File("some file");
    private final Integer someInt = 1;
    private final Sample sampleObject = new Sample();


    public ThreadedProducerTemplate(int producerThreadCount) {
        this.producerThreadCount = Parameters.threadCountProducer();
        testSize = Parameters.duration() > 0 ? Parameters.duration() : Integer.MAX_VALUE;

        executorService = Executors.newFixedThreadPool(producerThreadCount);
        targetRate = Parameters.targetRate() ;
    }

    private static long getExchangeInterval(final long rate) {
        return rate > 0 ? (1_000_000_000L / rate) : 0;
    }

    private long getIntervalInNanos() {
        final long intervalInNanos = getExchangeInterval(targetRate);

        if (intervalInNanos > 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Producer {} has started firing events with an interval of {} ns and rate of {} msg/sec",
                        Thread.currentThread().getId(), intervalInNanos, targetRate);
            }
        } else if (targetRate == 0) {
            LOG.debug("Producer {} has started firing events with an unbounded rate",
                    Thread.currentThread().getId());
        }

        return intervalInNanos;
    }

    public static long waitNanoInterval(final long expectedFireTime, final long intervalInNanos) {
        assert intervalInNanos > 0;
        long now;
        do {
            now = System.nanoTime();
            if ((now - expectedFireTime) < 0) {
                LockSupport.parkNanos(expectedFireTime - now);
            }
        } while ((now - expectedFireTime) < 0);

        return now;
    }

    protected void produceMessagesWithRate(int numMessages, ProducerTemplate producerTemplate, Endpoint endpoint) {
        final long intervalInNanos = getIntervalInNanos();
        long nextFireTime = System.nanoTime() + intervalInNanos;

        LOG.info("Sending message {} from {} with rate {}", numMessages, Thread.currentThread().getId(), targetRate);


        List<Object> data = List.of("test-string", someFile, someInt, sampleObject);

        while (numMessages > 0) {
            if (intervalInNanos > 0) {
                final long now = waitNanoInterval(nextFireTime, intervalInNanos);
                assert (now - nextFireTime) >= 0 : "can't wait less than the configured interval in nanos";
                nextFireTime += intervalInNanos;
            }

            Object payload = data.get(numMessages % data.size());
            producerTemplate.sendBody(endpoint, payload);

            numMessages--;
        }

        System.exit(0);
    }

    protected abstract void produceMessagesWithRate(int numMessages);

    protected void produceMessages(int numMessages, ProducerTemplate producerTemplate, Endpoint endpoint) {
        LOG.info("Sending {} messages from {}", numMessages, Thread.currentThread().getId());
        List<Object> data = List.of("test-string", someFile, someInt, sampleObject);

        for (int i = 0; i < numMessages; i++) {
            Object payload = data.get(i % data.size());
            producerTemplate.sendBody(endpoint, payload);
        }
    }

    protected abstract void produceMessages(int numMessages);

    protected void produce(Exchange exchange) {
        if (targetRate == 0) {
            for (int i = 0; i < producerThreadCount; i++) {
                executorService.submit(() -> produceMessages(testSize / producerThreadCount));
            }
        } else {
            for (int i = 0; i < producerThreadCount; i++) {
                executorService.submit(() -> produceMessagesWithRate(testSize / producerThreadCount));
            }
        }
    }

    public int getProducerThreadCount() {
        return producerThreadCount;
    }

    public int getTestSize() {
        return testSize;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public int getTargetRate() {
        return targetRate;
    }

    public File getSomeFile() {
        return someFile;
    }

    public Integer getSomeInt() {
        return someInt;
    }

    public Sample getSampleObject() {
        return sampleObject;
    }
}
