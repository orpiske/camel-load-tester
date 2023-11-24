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

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.component.dataset.SimpleDataSet;
import org.apache.camel.main.Main;

public final class DataSetHelper {
    public static final String DATASET_NAME = "testSet";

    private DataSetHelper() {

    }

    public static void bindDataSet(Main main, int testSize) {
        bindDataSet(main, testSize, null);
    }

    public static void bindDataSet(Main main, int testSize, Processor outputTransformer) {
        SimpleDataSet simpleDataSet = new SimpleDataSet();

        simpleDataSet.setDefaultBody(Boolean.TRUE);
        simpleDataSet.setSize(testSize);
        simpleDataSet.setDefaultBody("{\"value\":\"data\"}");

        if (outputTransformer != null) {
            simpleDataSet.setOutputTransformer(outputTransformer);
        }

        main.bind(DATASET_NAME, simpleDataSet);
    }

    public static SimpleDataSet lookupDataSet(CamelContext context) {
        return context.getRegistry().lookupByNameAndType(DataSetHelper.DATASET_NAME, SimpleDataSet.class);
    }

}
