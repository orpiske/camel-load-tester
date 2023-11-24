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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.load.tester.common.IOUtil;
import org.apache.camel.load.tester.common.types.BaselinedTestMetrics;
import org.apache.camel.load.tester.common.types.TestMetrics;

public class JsonOutputHandler implements OutputHandler {
    private final String outputDir;
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonOutputHandler(String outputDir) {
        this.outputDir = outputDir;
    }


    @Override
    public void output(TestMetrics testMetrics) {
        try {
            final String body = mapper.writeValueAsString(testMetrics);

            Path outputPath = Path.of(outputDir, IOUtil.FILE_RESULTS);

            Files.writeString(outputPath, body);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void output(BaselinedTestMetrics testMetrics) {
        try {
            final String body = mapper.writeValueAsString(testMetrics);

            Path outputPath = Path.of(outputDir, IOUtil.FILE_BASELINE);

            Files.writeString(outputPath, body);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
