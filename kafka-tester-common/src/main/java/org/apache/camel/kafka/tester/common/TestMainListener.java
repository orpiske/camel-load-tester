package org.apache.camel.kafka.tester.common;

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
        int threadCount = Parameters.threadCount();
        final CamelContext camelContext = main.getCamelContext();
        final ThreadPoolProfile defaultThreadPoolProfile = camelContext.getExecutorServiceManager().getDefaultThreadPoolProfile();

        if (threadCount >= defaultThreadPoolProfile.getMaxPoolSize()) {
            // This is usually a very bad idea, but it allow us to try some scenarios with over commit of up to 50%
            final double increment = defaultThreadPoolProfile.getMaxPoolSize() * 0.5;
            final int maxPoolSize = defaultThreadPoolProfile.getMaxPoolSize() + (int) increment;
            LOG.warn("The test is enabling over committing of resources by increasing the max pool size from {} to {}",
                    defaultThreadPoolProfile.getMaxPoolSize(), maxPoolSize);
            defaultThreadPoolProfile.setMaxPoolSize(maxPoolSize);
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

    @Override
    public void configure(CamelContext context) {
        // NO-OP

    }
}
