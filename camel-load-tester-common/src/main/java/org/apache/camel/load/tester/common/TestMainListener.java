package org.apache.camel.load.tester.common;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.main.BaseMainSupport;
import org.apache.camel.main.MainListener;
import org.apache.camel.spi.ThreadPoolProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMainListener implements MainListener {
    private static final Logger LOG = LoggerFactory.getLogger(TestMainListener.class);
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    private final Reporter reporter;


    public TestMainListener(Reporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public void afterConfigure(BaseMainSupport main) {
        final CamelContext camelContext = main.getCamelContext();
        LOG.info("Running the test with Camel version {}", camelContext.getVersion());

        /*
         This is the number of other threads that may be active at any point during the test:
         1 for Camel main
         1 for the timer://start (when using the threaded producer)
         3 other ad-hoc parallel tasks that may be executed by the code at any point during the test
         */

        final int numOtherThreads = 5;
        int threadCountProcessor = Parameters.threadCountProcessor();
        int threadCountConsumer = Parameters.threadCountConsumer();
        int threadCountProducer = Parameters.threadCountProducer();

        final ThreadPoolProfile defaultThreadPoolProfile = camelContext.getExecutorServiceManager().getDefaultThreadPoolProfile();

        LOG.info("The system is configured with the thread pool: {}", defaultThreadPoolProfile.getId());
        LOG.info("The max pool size for the thread pool is: {}", defaultThreadPoolProfile.getMaxPoolSize());
        LOG.info("The pool size for the thread pool is: {}", defaultThreadPoolProfile.getPoolSize());
        LOG.info("The max queue size for the thread pool is: {}", defaultThreadPoolProfile.getMaxQueueSize());
        LOG.info("The processor thread count is: {}", threadCountProcessor);
        LOG.info("The consumer thread count is: {}", threadCountConsumer);
        LOG.info("The producer thread count is: {}", threadCountProducer);
        LOG.info("The keep alive time is: {}", defaultThreadPoolProfile.getKeepAliveTime());

        // See the note about on the numOtherThreads
        final int requiredPoolSize = (threadCountProcessor + threadCountConsumer + threadCountProducer) + numOtherThreads;
        if (requiredPoolSize >= defaultThreadPoolProfile.getMaxPoolSize()) {
            LOG.warn("The test is enabling over committing of resources by increasing the max pool size from {} to {}",
                    defaultThreadPoolProfile.getMaxPoolSize(), requiredPoolSize);
            defaultThreadPoolProfile.setMaxPoolSize(requiredPoolSize);

            LOG.info("The max pool size for the thread pool was adjusted to: {}", defaultThreadPoolProfile.getMaxPoolSize());
            if (requiredPoolSize / 2 > defaultThreadPoolProfile.getPoolSize()) {
                defaultThreadPoolProfile.setPoolSize(requiredPoolSize / 2);

                LOG.info("The pool size for the thread pool was adjusted to: {}", defaultThreadPoolProfile.getPoolSize());
            }
        }
    }

    @Override
    public void afterStart(BaseMainSupport main) {
        executorService.scheduleAtFixedRate(reporter::update,
        1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void afterStop(BaseMainSupport main) {
        reporter.closeReport();
        main.shutdown();
    }

    public void configure(BaseMainSupport main) {
        // NO-OP
    }

    public void configure(CamelContext context) {

    }


    @Override
    public void beforeConfigure(BaseMainSupport main) {
        // NO-OP
    }

    @Override
    public void beforeInitialize(BaseMainSupport main) {
        // NO-OP
    }

    @Override
    public void beforeStart(BaseMainSupport main) {
        executorService.scheduleWithFixedDelay(reporter::staledCheck,
                10, 10, TimeUnit.SECONDS);
    }


    @Override
    public void beforeStop(BaseMainSupport main) {
        executorService.shutdown();
    }
}
