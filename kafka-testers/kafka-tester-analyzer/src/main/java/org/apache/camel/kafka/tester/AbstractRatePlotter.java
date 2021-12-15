package org.apache.camel.kafka.tester;

import java.awt.*;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.colors.ChartColor;

/**
 * Base class for plotters
 */
public abstract class AbstractRatePlotter {
    private int outputWidth = 1200;
    private int outputHeight = 700;
    private boolean plotGridLinesVisible = true;

    private ChartProperties chartProperties = new ChartProperties();

    public static class SeriesData {
        String seriesName;
        List<? extends Number> yData;
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
     * Sets the the grid lines should be visible
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


    /**
     * Build a default chart
     * @return
     */
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

        chart.getStyler().setPlotBackgroundColor(ChartColor.getAWTColor(ChartColor.WHITE));
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setChartTitleBoxBackgroundColor(new Color(0, 222, 0));

        Font font = new Font("Verdana", Font.PLAIN, 12);
        chart.getStyler().setBaseFont(font);
        chart.getStyler().setPlotGridLinesVisible(plotGridLinesVisible);

        chart.getStyler().setXAxisLabelRotation(45);

        chart.getStyler().setAxisTickMarkLength(15);
        chart.getStyler().setPlotMargin(0);
        chart.getStyler().setPlotContentSize(.95);
        chart.getStyler().setDatePattern("yyyy-MM-dd HH:mm:ss");

        chart.getStyler().setChartTitleFont(new Font("Verdana", Font.BOLD, 14));
        chart.getStyler().setLegendFont(new Font("Verdana", Font.PLAIN, 12));
        chart.getStyler().setAxisTitleFont(new Font("Verdana", Font.PLAIN, 12));
        chart.getStyler().setAxisTickLabelsFont(new Font("Verdana", Font.PLAIN, 10));

        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
        chart.getStyler().setLegendLayout(Styler.LegendLayout.Vertical);

        return chart;
    }

    /**
     * Plots the data
     * @param xData
     * @param yData
     * @throws IOException
     */
    abstract public void plot(final List<Date> xData, final List<? extends Number> yData, SeriesData...extraYSeries) throws IOException;
}
