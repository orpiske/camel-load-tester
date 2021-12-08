package org.apache.camel.kafka.tester;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.camel.kafka.tester.io.common.FileHeader;
import org.apache.camel.kafka.tester.io.common.RateEntry;
import org.apache.camel.kafka.tester.io.writer.BinaryRateReader;

public class MainAnalyzer {

    public static void main(String[] args) throws IOException {
        String name = System.getProperty("test.file");

        try (BinaryRateReader rateReader = new BinaryRateReader(new File(name))) {
            FileHeader header = rateReader.getHeader();

            System.out.println("Analyzing: " + header.getTestSuiteVersion());
            System.out.println("Version: " + header.getFileVersion());
            System.out.println("Type: " + header.getRole().name());


            List<RateEntry> entries = new ArrayList<>();

            RateEntry entry = rateReader.readRecord();
            while (entry != null) {
                entries.add(entry);
                entry = rateReader.readRecord();
            }

            RateEntry minimal = entries.stream().min(Comparator.comparing(e -> e.getCount())).get();
            RateEntry maximum = entries.stream().max(Comparator.comparing(e -> e.getCount())).get();
            long total = entries.stream().collect(Collectors.summingLong(r -> r.getCount()));

            double average = (double) total / (double) entries.size();

            System.out.println("Minimum: " + minimal.getCount() + " at " + Instant.ofEpochSecond(minimal.getTimestamp()));
            System.out.println("Maximum: " + maximum.getCount() + " at " + Instant.ofEpochSecond(maximum.getTimestamp()));
            System.out.println("Average: " + average);
        }
    }
}
