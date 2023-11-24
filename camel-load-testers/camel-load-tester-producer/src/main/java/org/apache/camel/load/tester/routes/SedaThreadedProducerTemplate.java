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

import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.load.tester.common.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SedaThreadedProducerTemplate extends ThreadedProducerTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(SedaThreadedProducerTemplate.class);

    public SedaThreadedProducerTemplate() {
        super(Parameters.threadCountProducer());
    }

    protected void produceMessagesWithRate(int numMessages) {
        final ProducerTemplate producerTemplate = getCamelContext().createProducerTemplate();
        final Endpoint endpoint = getCamelContext().getEndpoint("seda:test?blockWhenFull=true&offerTimeout=1000");

        produceMessagesWithRate(numMessages, producerTemplate, endpoint);
    }


    protected void produceMessages(int numMessages) {
        final ProducerTemplate producerTemplate = getCamelContext().createProducerTemplate();
        final Endpoint endpoint = getCamelContext().getEndpoint("seda:test?blockWhenFull=true&offerTimeout=1000");

        produceMessages(numMessages, producerTemplate, endpoint);
    }

    @Override
    public void configure() {
        LOG.info("Using thread count for parallel production: {}", getProducerThreadCount());

        onException(IllegalStateException.class)
                .process(e -> LOG.error("The SEDA queue is likely full and the system may be unable to catch to the load. Fix the test parameters"));

        from("timer:start?repeatCount=1&delay=2000").to("direct:start");

        from("direct:start")
                .process(this::produce);
    }
}
