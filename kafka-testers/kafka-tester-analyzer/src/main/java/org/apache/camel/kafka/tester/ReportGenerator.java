package org.apache.camel.kafka.tester;

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

        File outputFile = templateParser.getOutputFile(new File(outputDir));

        try (FileWriter fw = new FileWriter(outputFile)) {
            templateParser.parse(fw);
            LOG.info("Template file was written to {}", outputFile);
        }
    }
}
