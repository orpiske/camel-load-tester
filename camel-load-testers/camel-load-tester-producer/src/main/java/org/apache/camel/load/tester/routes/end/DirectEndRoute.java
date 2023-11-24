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

package org.apache.camel.load.tester.routes.end;

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.load.tester.common.Counter;
import org.apache.camel.load.tester.common.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectEndRoute extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(DirectEndRoute.class);

    private final int threadCountProcessors;
    private final LongAdder longAdder;

    public DirectEndRoute() {
        this.threadCountProcessors = Parameters.threadCountProcessor();
        this.longAdder = Counter.getInstance().getAdder();
    }

    @Override
    public void configure() {
        LOG.info("Using thread count for parallel processing: {}", threadCountProcessors);

        if (threadCountProcessors == 0) {
            from("direct:test")
                    .routeId("noop-to-direct")
                    .process(exchange -> longAdder.increment());
        } else {
            from("direct:test")
                    .routeId("noop-to-direct-threaded")
                    .threads(threadCountProcessors)
                    .process(exchange -> longAdder.increment());
        }
    }
}
