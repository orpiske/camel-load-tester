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
import org.apache.camel.component.platform.http.vertx.VertxPlatformHttpServer;
import org.apache.camel.component.platform.http.vertx.VertxPlatformHttpServerConfiguration;
import org.apache.camel.load.tester.common.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Camel Java DSL Router
 */
public class PlatformHTTPConsumerRoute extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(PlatformHTTPConsumerRoute.class);
    private final VertxPlatformHttpServer vertxPlatformHttpServer;
    private final CountDownLatch latch;

    public PlatformHTTPConsumerRoute(CountDownLatch latch) {
        this.latch = latch;
        final int port = Parameters.httpPortConsumer();

        VertxPlatformHttpServerConfiguration conf = new VertxPlatformHttpServerConfiguration();
        conf.setBindPort(port);

        vertxPlatformHttpServer = new VertxPlatformHttpServer(conf);
    }

    public void kill(Exchange exchange) {
        LOG.info("Killing the process");
        latch.countDown();
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() throws Exception {
        getCamelContext().addService(vertxPlatformHttpServer);

        from("platform-http:/hello")
            .transform(simple("Hello ${body}"));

        from("platform-http:/version")
                .transform(constant(getCamelContext().getVersion()));

        from("platform-http:/kill")
                .transform(constant("Killing the service"))
                .to("seda:kill");

        from("seda:kill")
                .process(this::kill);

    }

}
