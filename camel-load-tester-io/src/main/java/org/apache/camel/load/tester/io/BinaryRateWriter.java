package org.apache.camel.load.tester.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.Instant;

import org.apache.camel.load.tester.io.common.FileHeader;
import org.apache.camel.load.tester.io.common.RateEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinaryRateWriter implements RateWriter {
    private static final Logger LOG = LoggerFactory.getLogger(BinaryRateWriter.class);

    private final FileChannel fileChannel;
    private long last = 0;

    // TODO: size needs to be adjusted accordingly
    private final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(FileHeader.BYTES + (RateEntry.BYTES * 60));

    /**
     * Constructor
     * @param reportFile the rate report file name
     * @param fileHeader the file header
     * @throws IOException in case of I/O errors
     */
    public BinaryRateWriter(final File reportFile, final FileHeader fileHeader) throws IOException {
        fileChannel = new FileOutputStream(reportFile).getChannel();

        writeHeader(fileHeader);
    }

    private void write() throws IOException {
        byteBuffer.flip();

        while (byteBuffer.hasRemaining()) {
            fileChannel.write(byteBuffer);
        }

        byteBuffer.flip();
        byteBuffer.clear();
    }


    private void writeHeader(final FileHeader header) throws IOException {
        byteBuffer.clear();
        byteBuffer.put(header.getFormatName().getBytes());
        byteBuffer.putInt(header.getFileVersion());
        byteBuffer.putInt(FileHeader.VERSION_NUMERIC);
        byteBuffer.putInt(header.getRole().getCode());

        write();
    }


    /**
     * Writes a performance entry to the file
     * @param metadata entry metadata
     * @param count rate
     * @param now timestamp of rate collection (as epoch seconds)
     * @throws IOException for multiple types of I/O errors
     */
    public RecordState write(int metadata, long count, long now) throws IOException {
        checkBufferCapacity();

        final RecordState state = checkRecordTimeSlot(now);

        if (state == RecordState.CURRENT) {
            byteBuffer.putInt(metadata);
            byteBuffer.putLong(count);
            byteBuffer.putLong(now);
            last = now;
        }

        return state;
    }

    public RecordState write(int metadata, long count, Instant timestamp) throws IOException {
        return write(metadata, count, timestamp.getEpochSecond());
    }

    private void checkBufferCapacity() throws IOException {
        final int remaining = byteBuffer.remaining();

        if (remaining < RateEntry.BYTES) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("There is not enough space on the buffer for a rate entry: {}", remaining);
            }

            write();
        }
    }

    private RecordState checkRecordTimeSlot(long now) {
        if (now <= last) {
            if (now < last) {
                return RecordState.OUTDATED;
            }

            return RecordState.DUPLICATED;
        }
        else {
            long next = last + 1;
            if (now != next && last != 0) {
                LOG.warn("Trying to save a non-sequential record: now {} / expected {}", now, next);
            }
        }

        return RecordState.CURRENT;
    }

    /**
     * Flushes the data to disk
     * @throws IOException in case of I/O errors
     */
    public void flush() throws IOException {
        write();
        fileChannel.force(true);
    }

    @Override
    public void close() {
        try {
            flush();
            fileChannel.close();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public RecordState tryWrite(int metadata, long count, Instant instant) throws IOException {
        long timestamp = instant.getEpochSecond();
        if (timestamp > last) {
            return write(metadata, count, timestamp);
        }

        return RecordState.OUTDATED;
    }
}
