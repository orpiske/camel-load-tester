package org.apache.camel.kafka.tester.io.common;

import org.apache.commons.lang3.StringUtils;

public class FileHeader {
    public static final int FORMAT_NAME_SIZE = 8;

    private final String formatName;
    private final int fileVersion;
    private final int camelVersion;
    private final Role role;

    public static final int VERSION_NUMERIC;
    public static final String FORMAT_NAME = "maestro";
    public static final int CURRENT_FILE_VERSION = 1;

    public static final FileHeader WRITER_DEFAULT_PRODUCER;
    public static final FileHeader WRITER_DEFAULT_CONSUMER;
    public static final int BYTES;

    static {
        String camelVersion = System.getProperty("camel.version");
        VERSION_NUMERIC = Integer.parseInt(camelVersion.replace(".", "").replaceAll("[a-zA-Z-]",""));

        WRITER_DEFAULT_PRODUCER = new FileHeader(FORMAT_NAME, CURRENT_FILE_VERSION,
                VERSION_NUMERIC, Role.PRODUCER);

        WRITER_DEFAULT_CONSUMER = new FileHeader(FORMAT_NAME, CURRENT_FILE_VERSION,
                VERSION_NUMERIC, Role.CONSUMER);

        // The underlying format for the role is an integer
        BYTES = FORMAT_NAME_SIZE + Integer.BYTES + Integer.BYTES + Integer.BYTES;
    }

    public FileHeader(final String formatName, int fileVersion, int camelVersion, Role role) {
        if (formatName == null || formatName.isEmpty() || formatName.length() > FORMAT_NAME_SIZE) {
            throw new IllegalArgumentException("The format name '" +
                    (formatName == null ? "null" : formatName) + "' is not valid");
        }

        if (formatName.length() < FORMAT_NAME_SIZE) {
            this.formatName = StringUtils.leftPad(formatName, FORMAT_NAME_SIZE);
        }
        else {
            this.formatName = formatName;
        }

        this.fileVersion = fileVersion;
        this.camelVersion = camelVersion;
        this.role = role;
    }

    public String getFormatName() {
        return formatName;
    }

    public int getFileVersion() {
        return fileVersion;
    }

    public int getCamelVersion() {
        return camelVersion;
    }

    public Role getRole() {
        return role;
    }
}
