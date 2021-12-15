package org.apache.camel.kafka.tester.io;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.apache.camel.kafka.tester.io.common.FileHeader;
import org.apache.camel.kafka.tester.io.common.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class BinaryRateReaderTest {
    private File reportFile;

    @AfterEach
    void clean() {
        if (reportFile != null && reportFile.exists()) {
            reportFile.delete();
        }
    }

    void generateDataFilePredictable() throws IOException {
        try (BinaryRateWriter binaryRateWriter = new BinaryRateWriter(reportFile, FileHeader.WRITER_DEFAULT_PRODUCER)) {
            long total = TimeUnit.DAYS.toSeconds(1);

            Instant now = Instant.now();

            for (int i = 0; i < total; i++) {
                binaryRateWriter.write(0, i + 1, now);

                now = now.plusSeconds(1);
            }
        }
    }



    @Test
    public void testHeader() {
        String path = this.getClass().getResource(".").getPath();
        reportFile = new File(path, "testHeader.dat");

        Assertions.assertDoesNotThrow(() -> generateDataFilePredictable());

        try (BinaryRateReader reader = new BinaryRateReader(reportFile)) {

            FileHeader fileHeader = reader.getHeader();
            assertEquals(FileHeader.FORMAT_NAME, fileHeader.getFormatName().trim());
            assertEquals(FileHeader.CURRENT_FILE_VERSION, fileHeader.getFileVersion());
            assertEquals(FileHeader.VERSION_NUMERIC, fileHeader.getCamelVersion());
            assertEquals(Role.PRODUCER, fileHeader.getRole());
        } catch (IOException e) {
            Assertions.fail(e.getMessage());
        }
    }
}
