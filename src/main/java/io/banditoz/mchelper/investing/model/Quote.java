package io.banditoz.mchelper.investing.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Quote {
    @JsonProperty("c")
    private double currentPrice;

    @JsonProperty("d")
    private double change;

    @JsonProperty("dp")
    private double changePercent;

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
        return change;
    }

    public double getChangePercent() {
        return changePercent;
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