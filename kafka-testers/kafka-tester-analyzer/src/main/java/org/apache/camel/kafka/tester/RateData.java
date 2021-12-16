package org.apache.camel.kafka.tester;

import java.util.List;

import org.apache.camel.kafka.tester.io.common.FileHeader;
import org.apache.camel.kafka.tester.io.common.RateEntry;

class RateData {
    FileHeader header;
    List<RateEntry> entries;
}
