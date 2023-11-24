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

package org.apache.camel.load.analyzer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.camel.CamelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(ReportGenerator.class);

    private final Properties properties;
    private final String outputDir;


    public ReportGenerator(Properties properties, String outputDir) {
        this.properties = properties;
        this.outputDir = outputDir;
    }

    public void generate() throws IOException, CamelException {
        VelocityTemplateParser templateParser = new VelocityTemplateParser(properties);

        final File outputDirFile = new File(outputDir);
        File outputFile = templateParser.getOutputFile(outputDirFile);

        try (FileWriter fw = new FileWriter(outputFile)) {
            templateParser.parse(fw);
            LOG.info("Template file was written to {}", outputFile);
        }
    }
}
