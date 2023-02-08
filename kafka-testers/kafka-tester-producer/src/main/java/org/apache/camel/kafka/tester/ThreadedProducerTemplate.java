package org.apache.camel.kafka.tester;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.LockSupport;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadedProducerTemplate extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadedProducerTemplate.class);

    private final LongAdder longAdder;
    private final int threadCount;
    private final int testSize = Integer.parseInt(System.getProperty("camel.main.durationMaxMessages", "0"));
    private final ExecutorService executorService;
    private final int targetRate;

    public ThreadedProducerTemplate(LongAdder longAdder, int threadCount) {
        this.longAdder = longAdder;
        this.threadCount = threadCount;

        executorService = Executors.newFixedThreadPool(threadCount);
        targetRate = Integer.valueOf(System.getProperty("test.target.rate", "50000"));
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

    private void produceMessages(int numMessages) {
        final long intervalInNanos = getIntervalInNanos();
        final ProducerTemplate producerTemplate = getCamelContext().createProducerTemplate();
        long nextFireTime = System.nanoTime() + intervalInNanos;

        LOG.info("Sending message {} from {}", numMessages, Thread.currentThread().getId());

        while (numMessages > 0) {
            if (intervalInNanos > 0) {
                final long now = waitNanoInterval(nextFireTime, intervalInNanos);
                assert (now - nextFireTime) >= 0 : "can't wait less than the configured interval in nanos";
                nextFireTime += intervalInNanos;
            }

            producerTemplate.sendBody("seda:test", "test");

            numMessages--;
        }
    }

    private void produce(Exchange exchange) {
        for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> produceMessages(testSize / threadCount));
        }
    }

    @Override
    public void configure() {
        LOG.info("Using thread count for parallel production: {}", threadCount);

        from("timer:start?repeatCount=1&delay=2000").to("direct:start");

        from("direct:start")
                .process(this::produce);

        fromF("seda:test?concurrentConsumers=%s", threadCount * 2)
                .routeId("noop-to-seda")
                .setExchangePattern(ExchangePattern.InOnly)
                .process(exchange -> longAdder.increment());

    }
}
