package org.apache.camel.kafka.tester;

import java.io.File;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.dataset.SimpleDataSet;
import org.apache.camel.kafka.tester.common.IOUtil;
import org.apache.camel.kafka.tester.common.Parameters;
import org.apache.camel.kafka.tester.common.TestMainListener;
import org.apache.camel.kafka.tester.common.WriterReporter;
import org.apache.camel.kafka.tester.io.BinaryRateWriter;
import org.apache.camel.kafka.tester.io.RateWriter;
import org.apache.camel.kafka.tester.io.common.FileHeader;
import org.apache.camel.kafka.tester.routes.DirectEndRoute;
import org.apache.camel.kafka.tester.routes.Routes;
import org.apache.camel.kafka.tester.routes.SedaEndRoute;
import org.apache.camel.main.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.kafka.tester.common.Parameters.threadCount;

/**
 * A Camel Application
 */
public class MainProducer {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) {
        Main main = new Main();
        final String testRateFileName = System.getProperty(Parameters.TEST_RATE_FILE, "producer-rate.data");

        int testSize = Integer.parseInt(System.getProperty(Parameters.CAMEL_MAIN_DURATION_MAX_MESSAGES, "0"));
        if (testSize == 0) {
            testSize = Integer.MAX_VALUE - 1;
        }

        bindDataSet(main, testSize);

        File testRateFile = IOUtil.create(testRateFileName);

        try (RateWriter rateWriter = new BinaryRateWriter(testRateFile, FileHeader.WRITER_DEFAULT_PRODUCER)) {
            int threadCount = threadCount();
            main.configure().addRoutesBuilder(new SedaEndRoute(threadCount));
            main.configure().addRoutesBuilder(new DirectEndRoute());

            RouteBuilder routeBuilder = Routes.getRouteBuilder();
            main.configure().addRoutesBuilder(routeBuilder);

            WriterReporter writerReporter;
            String onCompleteAction = System.getProperty(Parameters.TEST_ON_COMPLETE_ACTION, "do-nothing");
            if (onCompleteAction.equals("exit")) {
                writerReporter = new WriterReporter(rateWriter, testSize, main::stop, MainProducer::forceExit);
            } else {
                writerReporter = new WriterReporter(rateWriter, testSize, main::stop, null);
            }


            main.addMainListener(new TestMainListener(writerReporter));

            main.run();
        } catch (Exception e) {
            Logger log = LoggerFactory.getLogger(MainProducer.class);
            log.error("Unable to launch the test application: {}", e.getMessage(), e);
            main.shutdown();

            System.exit(1);
        }
    }

    private static void bindDataSet(Main main, int testSize) {
        SimpleDataSet simpleDataSet = new SimpleDataSet();

        simpleDataSet.setDefaultBody(Boolean.TRUE);
        simpleDataSet.setSize(testSize);
        simpleDataSet.setDefaultBody("{\"value\":\"data\"}");

        main.bind("testSet", simpleDataSet);
    }

    private static void forceExit(long messages) {
        System.out.println("Forcing exit after receiving " + messages + " messages");
        System.exit(0);
    }
}

