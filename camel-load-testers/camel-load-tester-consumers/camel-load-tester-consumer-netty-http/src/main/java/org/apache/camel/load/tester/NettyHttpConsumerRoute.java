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

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.load.tester.common.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyHttpConsumerRoute extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(NettyHttpConsumerRoute.class);
    private final CountDownLatch latch;

    public NettyHttpConsumerRoute(CountDownLatch latch) {
        this.latch = latch;
    }

    public void kill(Exchange exchange) {
        LOG.info("Killing the process");
        latch.countDown();
    }

    @Override
    public void configure() throws Exception {
        final int port = Parameters.httpPortConsumer();

        restConfiguration().component("netty-http")
                .host("0.0.0.0")
                .port(port);

        rest("/")
                .post("/hello").to("seda:hello")
                .get("/version").to("seda:version")
                .get("/kill").to("seda:kill");


        from("seda:hello")
                .routeId("hello")
                .transform(simple("Hello ${body}"));

        from("seda:version")
                .routeId("version")
                .transform(constant(getCamelContext().getVersion()));

        from("seda:kill")
                .routeId("kill")
                .process(this::kill);
    }
}
