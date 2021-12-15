package org.apache.camel.kafka.tester.common;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.CamelContext;
import org.apache.camel.kafka.tester.io.RateWriter;
import org.apache.camel.main.BaseMainSupport;
import org.apache.camel.main.MainListener;

public class TestMainListener implements MainListener {


    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    private final Reporter reporter;


    public TestMainListener(RateWriter writer, LongAdder longAdder, long testSize, Reporter.Action stopAction) {
        this.reporter = new Reporter(writer, longAdder, testSize, stopAction);
    }

    @Override
    public void afterConfigure(BaseMainSupport main) {
        // NO-OP
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
