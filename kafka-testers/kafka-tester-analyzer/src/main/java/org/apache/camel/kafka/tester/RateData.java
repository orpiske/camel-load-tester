package org.apache.camel.kafka.tester;

import java.util.List;

import org.apache.camel.kafka.tester.io.common.FileHeader;
import org.apache.camel.kafka.tester.io.common.RateEntry;

public class RateData {
    public FileHeader header;
    public List<RateEntry> entries;
}
