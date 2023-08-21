package org.apache.camel.load.tester.common.types;

public class Metrics {
    private double total;
    private double minimum;
    private double maximum;
    private double mean;
    private double geoMean;
    private double stdDeviation;
    private double p50Latency;
    private double p90Latency;
    private double p95Latency;
    private double p99Latency;
    private double p999Latency;
    private long startTimeStamp;
    private long endTimeStamp;
    private double maxLatency;


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

    public double getP50Latency() {
        return p50Latency;
    }

    public void setP50Latency(double p50Latency) {
        this.p50Latency = p50Latency;
    }

    public double getP90Latency() {
        return p90Latency;
    }

    public void setP90Latency(double p90Latency) {
        this.p90Latency = p90Latency;
    }

    public double getP95Latency() {
        return p95Latency;
    }

    public void setP95Latency(double p95Latency) {
        this.p95Latency = p95Latency;
    }

    public double getP99Latency() {
        return p99Latency;
    }

    public void setP99Latency(double p99Latency) {
        this.p99Latency = p99Latency;
    }

    public double getP999Latency() {
        return p999Latency;
    }

    public void setP999Latency(double p999Latency) {
        this.p999Latency = p999Latency;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public void setStartTimeStamp(long startTimeStamp) {
        this.startTimeStamp = startTimeStamp;
    }

    public long getEndTimeStamp() {
        return endTimeStamp;
    }

    public void setEndTimeStamp(long endTimeStamp) {
        this.endTimeStamp = endTimeStamp;
    }

    public double getMaxLatency() {
        return maxLatency;
    }

    public void setMaxLatency(double maxLatency) {
        this.maxLatency = maxLatency;
    }
}
