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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.apache.camel.main.Main;

/**
 * A Camel Application
 */
public class MainConsumer {
    private static final CountDownLatch LATCH = new CountDownLatch(1);
    private static final Main MAIN = new Main();

    private static void shutdownExecutor() {
        try {
            LATCH.await();
        } catch (InterruptedException e) {
            // Ignored ... shutting down
        }

        MAIN.stop();

        System.exit(0);
    }

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
        Executors.newFixedThreadPool(1).submit(() -> shutdownExecutor());

        MAIN.configure().addRoutesBuilder(new PlatformHTTPConsumerRoute(LATCH));
        MAIN.run(args);
    }



}

