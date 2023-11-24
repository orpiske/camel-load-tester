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

package org.apache.camel.load.analyzer;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.HdrHistogram.Histogram;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.colors.ChartColor;

/**
 * A base class for HDR plotters
 */
public abstract class AbstractHdrPlotter {
    private int outputWidth = 1200;
    private int outputHeight = 700;
    private boolean plotGridLinesVisible = true;

    private ChartProperties chartProperties = new ChartProperties();

    public static class SeriesData {
        String seriesName;
        Histogram yData;
    }

    public static class HistogramXY {
        final List<Double> xData;
        final List<Long> yData;

        public HistogramXY() {
            xData = new ArrayList<>();
            yData = new ArrayList<>();
        }
    }

    /**
     * Sets the output width for the graph
     * @param outputWidth the width in pixels
     */
    public void setOutputWidth(int outputWidth) {
        this.outputWidth = outputWidth;
    }


    /**
     * Gets the output width
     * @return
     */
    public int getOutputWidth() {
        return outputWidth;
    }

    /**
     * Sets the output height for the graph
     * @param outputHeight the height in pixels
     */
    public void setOutputHeight(int outputHeight) {
        this.outputHeight = outputHeight;
    }


    /**
     * Gets the output height
     * @return
     */
    public int getOutputHeight() {
        return outputHeight;
    }


    /**
     * Sets the grid lines should be visible
     * @param plotGridLinesVisible true to make the grid lines visible or false otherwise
     */
    public void setPlotGridLinesVisible(boolean plotGridLinesVisible) {
        this.plotGridLinesVisible = plotGridLinesVisible;
    }


    public boolean isPlotGridLinesVisible() {
        return plotGridLinesVisible;
    }

    /**
     * Get the chart properties
     * @return
     */
    public ChartProperties getChartProperties() {
        return chartProperties;
    }

    /**
     * Set the chart properties
     * @param chartProperties
     */
    public void setChartProperties(ChartProperties chartProperties) {
        this.chartProperties = chartProperties;
    }


    protected XYChart buildCommonChart() {

        // Create Chart
        XYChart chart = new XYChartBuilder()
                .width(outputWidth)
                .height(outputHeight)
                .title(chartProperties.getTitle())
                .xAxisTitle(chartProperties.getxTitle())
                .yAxisTitle(chartProperties.getyTitle())
                .theme(Styler.ChartTheme.Matlab)
                .build();

        chart.getStyler().setPlotBackgroundColor(ChartColor.WHITE.getColor());
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setChartTitleBoxBackgroundColor(new Color(0, 222, 0));

        chart.getStyler().setPlotGridLinesVisible(plotGridLinesVisible);

        chart.getStyler().setXAxisMax(100.0);
        chart.getStyler().setXAxisLabelRotation(45);

        chart.getStyler().setAxisTickMarkLength(15);
        chart.getStyler().setPlotMargin(0);
        chart.getStyler().setPlotContentSize(.99);

        chart.getStyler().setChartTitleFont(new Font("Verdana", Font.BOLD, 14));
        chart.getStyler().setLegendFont(new Font("Verdana", Font.PLAIN, 12));
        chart.getStyler().setAxisTitleFont(new Font("Verdana", Font.PLAIN, 12));
        chart.getStyler().setAxisTickLabelsFont(new Font("Verdana", Font.PLAIN, 10));

        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
        chart.getStyler().setLegendLayout(Styler.LegendLayout.Vertical);

        return chart;
    }

    abstract public void plot(final Histogram data, SeriesData...extraYSeries) throws IOException;
}
