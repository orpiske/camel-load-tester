package org.apache.camel.kafka.tester;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.camel.kafka.tester.io.common.FileHeader;
import org.apache.camel.kafka.tester.io.common.RateEntry;

public class Plotter {
    private final String outputDir;

    public Plotter(String outputDir) {
        this.outputDir = outputDir;
    }

    public void plot(RateData testData) throws IOException {
        RatePlotter plotter = createStandardPlotter(testData.header);

        List<Date> xData = testData.entries.stream().map(r -> new Date(r.getTimestamp())).collect(Collectors.toList());
        List<Long> yData = testData.entries.stream().map(RateEntry::getCount).collect(Collectors.toList());
        plotter.plot(xData, yData);
    }

    public String plot(RateData testData, RateData baselineData) throws IOException {
        RatePlotter plotter = createStandardPlotter(testData.header);

        List<Date> xData = testData.entries.stream().map(r -> new Date(r.getTimestamp())).collect(Collectors.toList());
        List<Long> yDataTest = testData.entries.stream().map(RateEntry::getCount).collect(Collectors.toList());
        List<Long> yDataBaseline = baselineData.entries.stream().map(RateEntry::getCount).collect(Collectors.toList());

        AbstractRatePlotter.SeriesData seriesData = new AbstractRatePlotter.SeriesData();

        seriesData.seriesName = "Baseline " + baselineData.header.getCamelVersion();
        seriesData.yData = yDataBaseline;
        plotter.plot(xData, yDataTest, seriesData);
        return plotter.getFileName();
//        properties.put("rateFile", plotter.getFileName());
    }

    private RatePlotter createStandardPlotter(FileHeader testData) {
        RatePlotter plotter = new RatePlotter(outputDir, testData.getRole().toString().toLowerCase(Locale.ROOT));
        ChartProperties chartProperties = new ChartProperties();

        chartProperties.setSeriesName(ChartProperties.capitilizeOnly(testData.getRole().toString()) + " " + testData.getCamelVersion());
        plotter.setChartProperties(chartProperties);
        return plotter;
    }
}
