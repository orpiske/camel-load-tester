package org.apache.camel.kafka.tester.output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.kafka.tester.common.IOUtil;
import org.apache.camel.kafka.tester.common.types.BaselinedTestMetrics;
import org.apache.camel.kafka.tester.common.types.TestMetrics;

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
