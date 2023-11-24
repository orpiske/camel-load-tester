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

import java.io.File;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.load.tester.support.Sample;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Camel Java DSL Router
 */
public class DataSetInjectionToDirect extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(DataSetInjectionToDirect.class);
    private ProducerTemplate producerTemplate;
    private Endpoint endpoint;

    private void inject(Exchange exchange) {
        try {
            producerTemplate.sendBody(endpoint, 1);
            producerTemplate.sendBody(endpoint, "skip");
            producerTemplate.sendBody(endpoint, new File("a"));
        } catch (Exception e) {
            LOG.error("Error: {}", e.getMessage(), e);
        }
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
        producerTemplate = getContext().createProducerTemplate();
        endpoint = getContext().getEndpoint("direct:test");

        from("dataset:testSet?produceDelay=0&minRate={{?min.rate}}&initialDelay={{initial.delay:2000}}&dataSetIndex=off")
                .routeId("dataset-injection")
                .unmarshal().json(JsonLibrary.Jackson, Sample.class)
                .process(this::inject)
                .choice().when(body().contains("skip")).to("log: Skipped ${body}")
                .otherwise().to("direct:test");
    }
}
