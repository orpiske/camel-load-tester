/*
 *  Copyright 2017 Otavio Rodolfo Piske
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.camel.kafka.tester;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.colors.XChartSeriesColors;
import org.knowm.xchart.style.lines.SeriesLines;
import org.knowm.xchart.style.markers.SeriesMarkers;

public class RatePlotter extends AbstractRatePlotter {
    private final String outputDir;

    public RatePlotter(final String outputDir) {
        this.outputDir = outputDir;
    }

    private void plotAll(final List<Date> xData, final List<? extends Number> yData, SeriesData...extraYSeries) throws IOException {
        // Create Chart
        XYChart chart = buildCommonChart();

        // Series
        XYSeries series = chart.addSeries(getChartProperties().getSeriesName(), xData, yData);

        series.setLineColor(XChartSeriesColors.BLUE);
        series.setMarkerColor(Color.LIGHT_GRAY);
        series.setMarker(SeriesMarkers.NONE);
        series.setLineStyle(SeriesLines.SOLID);

        for (SeriesData seriesData : extraYSeries) {
            // Truncates the list, otherwise xchart complains
            if (seriesData.yData.size() < xData.size()) {
                while (seriesData.yData.size() != xData.size()) {
                    seriesData.yData.add(null);
                }
            } else {
                if (seriesData.yData.size() > xData.size()) {
                    seriesData.yData = seriesData.yData.subList(0, xData.size());
                }
            }
            chart.addSeries(seriesData.seriesName, xData, seriesData.yData);
        }

        BitmapEncoder.saveBitmap(chart, getFileName(), BitmapEncoder.BitmapFormat.PNG);
    }

    public String getFileName() {
        return outputDir + File.separator + "rate.png";
    }

    @Override
    public void plot(final List<Date> xData, final List<? extends Number> yData, SeriesData...extraYSeries) throws IOException {
        if (xData == null || xData.size() == 0) {
            throw new IllegalArgumentException("The 'X' column data set is empty");
        }

        if (yData == null || yData.size() == 0) {
            throw new IllegalArgumentException("The 'Y' column data set is empty");
        }

        plotAll(xData, yData, extraYSeries);
    }
}
