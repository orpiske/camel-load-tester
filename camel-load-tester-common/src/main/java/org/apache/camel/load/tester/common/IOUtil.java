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

package org.apache.camel.load.tester.common;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IOUtil {
    private static final Logger LOG = LoggerFactory.getLogger(IOUtil.class);

    public static final String FILE_RESULTS = "results.json";
    public static final String FILE_BASELINE = "baseline.json";

    private IOUtil() {}

    public static File create(String filePath) {
        File file = new File(filePath);

        File parent = file.getParentFile();
        if (parent != null) {
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    LOG.warn("Failed to create parent dirs for {}", parent);
                }
            }
        }

        return file;
    }
}
