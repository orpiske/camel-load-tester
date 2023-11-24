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

package org.apache.camel.load.tester.routes;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.load.tester.common.Counter;
import org.apache.camel.load.tester.common.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Camel Java DSL Router
 */
public class DataSetThreadedProcessor extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(DataSetThreadedProcessor.class);

    private final LongAdder longAdder;
    private final int threadCountProcessor;

    public DataSetThreadedProcessor() {
        this.longAdder = Counter.getInstance().getAdder();
        this.threadCountProcessor = Parameters.threadCountProcessor();
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        LOG.info("Using thread count for processors: {}", threadCountProcessor);

        if (threadCountProcessor == 0) {
            from("dataset:testSet?produceDelay=0&minRate={{?min.rate}}&initialDelay={{initial.delay:2000}}&dataSetIndex=off")
                    .routeId("dataset-single-threaded")
                    .process(exchange -> longAdder.increment());
        } else {
            from("dataset:testSet?produceDelay=0&minRate={{?min.rate}}&initialDelay={{initial.delay:2000}}&dataSetIndex=off")
                    .routeId("dataset-threaded")
                    .threads(threadCountProcessor)
                    .process(exchange -> longAdder.increment());
        }
    }
}
