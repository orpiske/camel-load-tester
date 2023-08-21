package org.apache.camel.load.analyzer;

import java.util.Locale;

public class ChartProperties {
    private String title = "Rate distribution over time";
    private String seriesName = "Throughput rate";
    private String xTitle = "";
    private String yTitle = "Messages p/ second";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public String getxTitle() {
        if (xTitle != null && xTitle.length() >= 2) {
            return capitilize(xTitle);
        }
        else {
            return xTitle;
        }
    }

    public void setxTitle(String xTitle) {
        this.xTitle = xTitle;
    }

    public String getyTitle() {
        if (yTitle != null && yTitle.length() >= 2) {
            return capitilize(yTitle);
        }
        else {
            return yTitle;
        }
    }

    public static String capitilize(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public static String capitilizeOnly(String text) {
        return text.substring(0, 1).toUpperCase() + text.toLowerCase(Locale.ROOT).substring(1);
    }

    public void setyTitle(String yTitle) {
        this.yTitle = yTitle;
    }
}
