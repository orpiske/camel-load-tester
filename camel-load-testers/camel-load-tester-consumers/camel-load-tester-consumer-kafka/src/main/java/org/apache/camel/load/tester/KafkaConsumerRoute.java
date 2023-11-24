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

import java.util.concurrent.atomic.LongAdder;

import org.apache.camel.builder.RouteBuilder;

/**
 * A Camel Java DSL Router
 */
public class KafkaConsumerRoute extends RouteBuilder {
    private final LongAdder longAdder;
    private final String topic;

    public KafkaConsumerRoute(LongAdder longAdder, String topic) {
        this.longAdder = longAdder;
        this.topic = topic;
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        fromF("kafka:%s?autoOffsetReset=earliest", topic)
                .routeId("kafka-noop")
                .process(exchange -> longAdder.increment());
    }

}
