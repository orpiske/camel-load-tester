package org.apache.camel.kafka.tester.output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.HdrHistogram.Histogram;
import org.apache.camel.kafka.tester.common.types.BaselinedTestMetrics;
import org.apache.camel.kafka.tester.common.types.TestMetrics;

public class JsonOutputHandler implements OutputHandler {
    private final String outputDir;
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonOutputHandler(String outputDir) {
        this.outputDir = outputDir;
    }


    @Override
    public void outputSingleAnalyzis(TestMetrics testMetrics) {
        try {
            final String body = mapper.writeValueAsString(testMetrics);

            Path outputPath = Path.of(outputDir, "results.json");

            Files.writeString(outputPath, body);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void outputWithBaseline(BaselinedTestMetrics testMetrics) {
        try {
            final String body = mapper.writeValueAsString(testMetrics);

            Path outputPath = Path.of(outputDir, "baseline-rate.json");

            Files.writeString(outputPath, body);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void outputHistogram(Histogram histogram) {
        // NO-OP
    }

    @Override
    public void outputHistogram(BaselinedTestMetrics testMetrics) {
        try {
            final String body = mapper.writeValueAsString(testMetrics);

            Path outputPath = Path.of(outputDir, "baseline-latency.json");

            Files.writeString(outputPath, body);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
