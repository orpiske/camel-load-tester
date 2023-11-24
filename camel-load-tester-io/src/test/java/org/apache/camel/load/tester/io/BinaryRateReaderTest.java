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

package org.apache.camel.load.tester.io;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.apache.camel.load.tester.io.common.FileHeader;
import org.apache.camel.load.tester.io.common.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        Assertions.assertDoesNotThrow(this::generateDataFilePredictable);

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
