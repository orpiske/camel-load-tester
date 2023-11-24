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

package org.apache.camel.load.analyzer.output;

import java.util.HashSet;
import java.util.Set;

import org.apache.camel.load.tester.common.types.BaselinedTestMetrics;
import org.apache.camel.load.tester.common.types.TestMetrics;

public class DelegateOutputHandler implements OutputHandler {
    private Set<OutputHandler> outputHandlers = new HashSet<>();

    public void add(OutputHandler handler) {
        outputHandlers.add(handler);
    }

    @Override
    public void output(TestMetrics testMetrics) {
        for (var handler : outputHandlers) {
            handler.output(testMetrics);
        }
    }

    @Override
    public void output(BaselinedTestMetrics testMetrics) {
        for (var handler : outputHandlers) {
            handler.output(testMetrics);
        }
    }
}
