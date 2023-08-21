package org.apache.camel.load.analyzer;

import java.util.List;

import org.apache.camel.load.tester.io.common.FileHeader;
import org.apache.camel.load.tester.io.common.RateEntry;

public class RateData {
    public FileHeader header;
    public List<RateEntry> entries;
}
