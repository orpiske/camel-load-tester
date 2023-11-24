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
import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.load.tester.common.IOUtil;
import org.apache.camel.load.tester.common.TestMainListener;
import org.apache.camel.load.tester.common.WriterReporter;
import org.apache.camel.load.tester.io.common.FileHeader;
import org.apache.camel.load.tester.io.BinaryRateWriter;
import org.apache.camel.load.tester.io.RateWriter;
import org.apache.camel.main.Main;

/**
 * A Camel Application
 */
public class MainConsumer {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
        Main main = new Main();

        final String topic = System.getProperty("kafka.topic", "test-topic-consumer");
        String name = System.getProperty("test.rate.file", "consumer-rate.data");

        LongAdder longAdder = new LongAdder();
        long testSize = Long.parseLong(System.getProperty("camel.main.durationMaxMessages", "0"));

        File reportFile = IOUtil.create(name);

        try (RateWriter rateWriter = new BinaryRateWriter(reportFile, FileHeader.WRITER_DEFAULT_CONSUMER)) {
            main.configure().addRoutesBuilder(new KafkaConsumerRoute(longAdder, topic));
            WriterReporter writerReporter = new WriterReporter(rateWriter, testSize, main::stop, null);

            main.addMainListener(new TestMainListener(writerReporter));
            main.run(args);
        }

    }



}

