package org.apache.camel.kafka.tester.common.types;

public class Metrics {
    private double total;
    private double minimum;
    private double maximum;
    private double mean;
    private double geoMean;
    private double stdDeviation;

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getMinimum() {
        return minimum;
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public double getMaximum() {
        return maximum;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getGeoMean() {
        return geoMean;
    }

    public void setGeoMean(double geoMean) {
        this.geoMean = geoMean;
    }

    public double getStdDeviation() {
        return stdDeviation;
    }

    public void setStdDeviation(double stdDeviation) {
        this.stdDeviation = stdDeviation;
    }
}
