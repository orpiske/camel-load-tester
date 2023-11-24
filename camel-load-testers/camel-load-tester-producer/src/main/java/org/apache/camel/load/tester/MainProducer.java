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

package org.apache.camel.load.tester;

import java.io.File;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.load.tester.common.IOUtil;
import org.apache.camel.load.tester.common.Parameters;
import org.apache.camel.load.tester.common.TestMainListener;
import org.apache.camel.load.tester.common.WriterReporter;
import org.apache.camel.load.tester.io.BinaryRateWriter;
import org.apache.camel.load.tester.io.RateWriter;
import org.apache.camel.load.tester.io.common.FileHeader;
import org.apache.camel.load.tester.routes.DataSetHelper;
import org.apache.camel.load.tester.routes.Routes;
import org.apache.camel.main.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        DataSetHelper.bindDataSet(main, testSize);

        File testRateFile = IOUtil.create(testRateFileName);

        try (RateWriter rateWriter = new BinaryRateWriter(testRateFile, FileHeader.WRITER_DEFAULT_PRODUCER)) {
            RouteBuilder endRoute = Routes.getEndRouteBuilder();
            if (endRoute != null) {
                main.configure().addRoutesBuilder(endRoute);
            }

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

    private static void forceExit(long messages) {
        System.out.println("Forcing exit after receiving " + messages + " messages");
        System.exit(0);
    }
}

