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

package org.apache.camel.load.tester.routes.eip;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.dataset.SimpleDataSet;
import org.apache.camel.load.tester.common.Counter;
import org.apache.camel.load.tester.routes.DataSetHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Camel Java DSL Router
 */
public class ToDHeaderTest extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ToDHeaderTest.class);
    private static final String DYNAMIC_HEADER = "DYNAMIC_DEST";
    private static long index = 0;

    private final LongAdder longAdder = Counter.getInstance().getAdder();

    private void noopProcess(Exchange exchange) {
        longAdder.increment();
    }


    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        final SimpleDataSet test = DataSetHelper.lookupDataSet(getCamelContext());
        test.setOutputTransformer(exchange -> {
            exchange.setProperty(DYNAMIC_HEADER, index % 2);
            index++;
        });

        onException(IllegalStateException.class)
                .process(e -> LOG.error("The SEDA queue is likely full and the system may be unable to catch to the load. Fix the test parameters: {}", e.getException().getMessage(), e.getException()));

        from("dataset:testSet?produceDelay=0&minRate={{?min.rate}}&initialDelay={{initial.delay:2000}}&dataSetIndex=lenient")
                .routeId("dataset-noop-header-tod")
                .toD("disruptor:test.${header[\"DYNAMIC_DEST\"]}");

        from("disruptor:test.0")
                .process(this::noopProcess);

        from("disruptor:test.1")
                .process(this::noopProcess);
    }


}
