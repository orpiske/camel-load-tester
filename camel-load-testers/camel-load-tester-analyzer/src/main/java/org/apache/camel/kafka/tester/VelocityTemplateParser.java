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
package org.apache.camel.kafka.tester;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

import org.apache.camel.CamelException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

public class VelocityTemplateParser {

    private final Properties reportProperties;

    public VelocityTemplateParser(Properties reportProperties) {
        this.reportProperties = reportProperties;

        initializeTemplateEngine();
    }

    private void initializeTemplateEngine() {
        Properties props = new Properties();

        props.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");

        props.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());

        Velocity.init(props);
    }

    public void parse(Writer writer) throws CamelException {
        VelocityContext context = new VelocityContext();

        try {
            loadTemplateProperties(context);
        } catch (IOException e) {
            throw new CamelException("Unable to load the template properties", e);
        }

        try {
            final String templatePath = "report.html.vm";
            Template template = Velocity.getTemplate(templatePath, "UTF-8");

            template.merge(context, writer);
        } catch (ResourceNotFoundException rnfe) {
            throw new CamelException("Could not find the template to parse", rnfe);
        } catch (ParseErrorException pee) {
            throw new CamelException("Failed parsing the template", pee);
        } catch (MethodInvocationException mie) {
            throw new CamelException("Method call within the templated has failed", mie);
        } catch (Exception e) {
            throw new CamelException("Unspecified error while loading, parsing or processing the template", e);
        }
    }

    private void loadTemplateProperties(VelocityContext context) throws IOException {
        reportProperties.forEach((k, v) -> context.put(k.toString(), v));
    }

    public File getOutputFile(File outputDir) throws IOException {
        File outputFile = new File(outputDir, "report.html");
        if (outputFile.exists()) {
            if (!outputFile.delete()) {
                throw new IOException("Unable to delete report: " + outputFile);
            }
        }

        return outputFile;
    }
}
