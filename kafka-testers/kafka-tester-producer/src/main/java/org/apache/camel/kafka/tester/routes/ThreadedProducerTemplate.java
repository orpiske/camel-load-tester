package org.apache.camel.kafka.tester.routes;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.kafka.tester.common.Parameters;
import org.apache.camel.kafka.tester.support.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadedProducerTemplate extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadedProducerTemplate.class);

    private final int threadCount;
    private final int testSize;
    private final ExecutorService executorService;
    private int targetRate;
    private File someFile = new File("some file");
    private Integer someInt = Integer.valueOf(1);
    private Sample sampleObject = new Sample();

    public ThreadedProducerTemplate() {
        this.threadCount = Parameters.threadCount();
        testSize = Parameters.duration() > 0 ? Parameters.duration() : Integer.MAX_VALUE;

        executorService = Executors.newFixedThreadPool(threadCount);
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

    private void produceMessagesWithRate(int numMessages) {
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

            producerTemplate.sendBody("seda:test?blockWhenFull=true&offerTimeout=1000", "test-string");
            producerTemplate.sendBody("seda:test?blockWhenFull=true&offerTimeout=1000", someFile);
            producerTemplate.sendBody("seda:test?blockWhenFull=true&offerTimeout=1000", someInt);
            producerTemplate.sendBody("seda:test?blockWhenFull=true&offerTimeout=1000", sampleObject);

            numMessages--;
        }

        System.exit(0);
    }

    private void produceMessages(int numMessages) {
        final ProducerTemplate producerTemplate = getCamelContext().createProducerTemplate();

        LOG.info("Sending message {} from {}", numMessages, Thread.currentThread().getId());

        for (int i = 0; i < numMessages; i++) {
            producerTemplate.sendBody("seda:test?blockWhenFull=true&offerTimeout=1000", "test-string");
            producerTemplate.sendBody("seda:test?blockWhenFull=true&offerTimeout=1000", someFile);
            producerTemplate.sendBody("seda:test?blockWhenFull=true&offerTimeout=1000", someInt);
            producerTemplate.sendBody("seda:test?blockWhenFull=true&offerTimeout=1000", sampleObject);
        }

    }

    private void produce(Exchange exchange) {
        if (targetRate == 0) {
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> produceMessages(testSize / threadCount));
            }
        } else {
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> produceMessagesWithRate(testSize / threadCount));
            }
        }
    }

    @Override
    public void configure() {
        LOG.info("Using thread count for parallel production: {}", threadCount);

        onException(IllegalStateException.class)
                .process(e -> LOG.error("The SEDA queue is likely full and the system may be unable to catch to the load. Fix the test parameters"));

        from("timer:start?repeatCount=1&delay=2000").to("direct:start");

        from("direct:start")
                .process(this::produce);
    }
}
