package org.apache.camel.kafka.tester.routes;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.kafka.tester.common.Parameters;
import org.apache.camel.spi.RouteStartupOrder;
import org.apache.camel.spi.ShutdownStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadedProducerTemplate extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadedProducerTemplate.class);

    private final int threadCount;
    private final int testSize;
    private final ExecutorService executorService;
    private int targetRate;

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

            producerTemplate.sendBody("seda:test?blockWhenFull=true&offerTimeout=1000", "test");

            numMessages--;
        }

        System.exit(0);
    }

    private void produceMessages(int numMessages) {
        final ProducerTemplate producerTemplate = getCamelContext().createProducerTemplate();

        LOG.info("Sending message {} from {}", numMessages, Thread.currentThread().getId());

        for (int i = 0; i < numMessages; i++) {
            producerTemplate.sendBody("seda:test?blockWhenFull=true&offerTimeout=1000", "test");
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

        getCamelContext().setShutdownStrategy(new ShutdownStrategy() {
            @Override
            public void shutdownForced(CamelContext context, List<RouteStartupOrder> routes) throws Exception {

            }

            @Override
            public void shutdown(CamelContext context, List<RouteStartupOrder> routes) throws Exception {

            }

            @Override
            public void suspend(CamelContext context, List<RouteStartupOrder> routes) throws Exception {

            }

            @Override
            public void shutdown(CamelContext context, List<RouteStartupOrder> routes, long timeout, TimeUnit timeUnit) throws Exception {

            }

            @Override
            public boolean shutdown(CamelContext context, RouteStartupOrder route, long timeout, TimeUnit timeUnit, boolean abortAfterTimeout) throws Exception {
                return false;
            }

            @Override
            public void suspend(CamelContext context, List<RouteStartupOrder> routes, long timeout, TimeUnit timeUnit) throws Exception {

            }

            @Override
            public void setTimeout(long timeout) {

            }

            @Override
            public long getTimeout() {
                return 0;
            }

            @Override
            public void setTimeUnit(TimeUnit timeUnit) {

            }

            @Override
            public TimeUnit getTimeUnit() {
                return null;
            }

            @Override
            public void setSuppressLoggingOnTimeout(boolean suppressLoggingOnTimeout) {

            }

            @Override
            public boolean isSuppressLoggingOnTimeout() {
                return false;
            }

            @Override
            public void setShutdownNowOnTimeout(boolean shutdownNowOnTimeout) {

            }

            @Override
            public boolean isShutdownNowOnTimeout() {
                return false;
            }

            @Override
            public void setShutdownRoutesInReverseOrder(boolean shutdownRoutesInReverseOrder) {

            }

            @Override
            public boolean isShutdownRoutesInReverseOrder() {
                return false;
            }

            @Override
            public void setLogInflightExchangesOnTimeout(boolean logInflightExchangesOnTimeout) {

            }

            @Override
            public boolean isLogInflightExchangesOnTimeout() {
                return false;
            }

            @Override
            public boolean isForceShutdown() {
                return false;
            }

            @Override
            public boolean hasTimeoutOccurred() {
                return false;
            }

            @Override
            public LoggingLevel getLoggingLevel() {
                return null;
            }

            @Override
            public void setLoggingLevel(LoggingLevel loggingLevel) {

            }

            @Override
            public void start() {

            }

            @Override
            public void stop() {

            }
        });

        onException(IllegalStateException.class)
                .process(e -> LOG.error("The SEDA queue is likely full and the system may be unable to catch to the load. Fix the test parameters"));

        from("timer:start?repeatCount=1&delay=2000").to("direct:start");

        from("direct:start")
                .process(this::produce);
    }
}
