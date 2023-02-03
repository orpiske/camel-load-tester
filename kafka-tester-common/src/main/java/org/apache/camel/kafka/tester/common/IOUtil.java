package org.apache.camel.kafka.tester.common;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IOUtil {
    private static final Logger LOG = LoggerFactory.getLogger(IOUtil.class);

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
