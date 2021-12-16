/*
 * Copyright 2018 Otavio Rodolfo Piske
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.kafka.tester.io;

import org.apache.camel.kafka.tester.io.common.FileHeader;
import org.apache.camel.kafka.tester.io.common.InvalidRecordException;
import org.apache.camel.kafka.tester.io.common.RateEntry;
import org.apache.camel.kafka.tester.io.common.RecordOverwriteException;
import org.apache.camel.kafka.tester.io.common.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class BinaryRateWriterTest {
    private File reportFile;

    void clean() {
        if (reportFile != null && reportFile.exists()) {
            reportFile.delete();
        }
    }

    void generateDataFilePredictable() throws IOException {
        String path = this.getClass().getResource(".").getPath();
        reportFile = new File(path, "testHeader.dat");

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
        Assertions.assertDoesNotThrow(() -> generateDataFilePredictable());
        Assumptions.assumeTrue(reportFile.exists());

        try (BinaryRateReader reader = new BinaryRateReader(reportFile)) {

            FileHeader fileHeader = reader.getHeader();
            assertEquals(FileHeader.FORMAT_NAME, fileHeader.getFormatName().trim());
            assertEquals(FileHeader.CURRENT_FILE_VERSION, fileHeader.getFileVersion());
            assertEquals(FileHeader.VERSION_NUMERIC, fileHeader.getCamelVersion());
            assertEquals(Role.PRODUCER, fileHeader.getRole());
        } catch (IOException e) {
            Assertions.fail(e.getMessage());
        } finally {
            clean();
        }
    }


    @Test
    public void testHeaderReadWriteRecords() throws IOException {
        Assertions.assertDoesNotThrow(() -> generateDataFilePredictable());
        Assumptions.assumeTrue(reportFile.exists());

        try (BinaryRateReader reader = new BinaryRateReader(reportFile)) {

            FileHeader fileHeader = reader.getHeader();
            assertEquals(FileHeader.FORMAT_NAME, fileHeader.getFormatName().trim());
            assertEquals(FileHeader.CURRENT_FILE_VERSION, fileHeader.getFileVersion());
            assertEquals(FileHeader.VERSION_NUMERIC, fileHeader.getCamelVersion());
            assertEquals(Role.PRODUCER, fileHeader.getRole());

            long count = 0;
            RateEntry entry = reader.readRecord();
            while (entry != null) {
                count++;
                entry = reader.readRecord();

                if (entry != null) {
                    assertEquals(0, entry.getMetadata());
                    assertNotEquals(0, entry.getCount());
                    assertNotEquals(0, entry.getTimestamp());
                }
            }

            Assertions.assertEquals(TimeUnit.DAYS.toSeconds(1), count, "The number of records don't match");
        }
        finally {
            clean();
        }
    }


    @Test
    public void testHeaderWriteRecordsNonSequential() throws IOException {
        String path = this.getClass().getResource(".").getPath();
        File reportFile = new File(path, "testHeaderWriteRecordsNonSequential.dat");

        try (BinaryRateWriter binaryRateWriter = new BinaryRateWriter(reportFile, FileHeader.WRITER_DEFAULT_PRODUCER)) {

            Instant now = Instant.now();

            Assertions.assertDoesNotThrow(() -> binaryRateWriter.write(0, 1, now));
            // Should throw RecordOverwriteException if trying to save a record for the same timestamp
            Assertions.assertThrows(RecordOverwriteException.class,
                    () -> binaryRateWriter.write(0, 1, now));

            // Should throw InvalidRecordException if trying to save a record for the same timestamp
            Assertions.assertThrows(InvalidRecordException.class,
                    () -> binaryRateWriter.write(0, 1, now.minusSeconds(1)));
        }
        finally {
            clean();
        }
    }
}


