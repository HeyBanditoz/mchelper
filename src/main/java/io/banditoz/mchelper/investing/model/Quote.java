package io.banditoz.mchelper.investing.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Quote {
    @JsonProperty("c")
    private double currentPrice;

    @JsonProperty("pc")
    private double previousClose;

    @JsonProperty("t")
    private int unixTime;

    @JsonProperty("h")
    private double high;

    @JsonProperty("l")
    private double low;

    @JsonProperty("o")
    private double open;

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getPreviousClose() {
        return previousClose;
    }

    public int getUnixTime() {
        return unixTime;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getOpen() {
        return open;
    }

    public double getChange() {
        return (double) Math.round((currentPrice - previousClose) * 100D) / 100D;
    }

    public double getChangePercent() {
        double changePercent = 100 * (currentPrice - previousClose) / previousClose;
        return Math.round(changePercent * 100D) / 100D; // round to two decimal places, faster than DecimalFormat
    }

    @Override
    public String toString() {
        return "Quote{" +
                "currentPrice=" + currentPrice +
                ", previousClose=" + previousClose +
                ", unixTime=" + unixTime +
                ", high=" + high +
                ", low=" + low +
                ", open=" + open +
                ", changePercent=" + getChangePercent() +
                '}';
    }
}