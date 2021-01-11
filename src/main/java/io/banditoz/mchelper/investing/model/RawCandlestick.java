package io.banditoz.mchelper.investing.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Date;

public class RawCandlestick {
    @JsonProperty("c")
    private ArrayList<Double> closes;

    @JsonProperty("s")
    private String status;

    @JsonProperty("t")
    private long[] unixTimestamps;

    @JsonProperty("v")
    private ArrayList<Double> volumes;

    @JsonProperty("h")
    private ArrayList<Double> highs;

    @JsonProperty("l")
    private ArrayList<Double> lows;

    @JsonProperty("o")
    private ArrayList<Double> opens;

    public ArrayList<Double> getCloses() {
        return closes;
    }

    public String getStatus() {
        return status;
    }

    public long[] getUnixTimestamps() {
        return unixTimestamps;
    }

    public ArrayList<Double> getVolumes() {
        return volumes;
    }

    public ArrayList<Double> getHighs() {
        return highs;
    }

    public ArrayList<Double> getLows() {
        return lows;
    }

    public ArrayList<Double> getOpens() {
        return opens;
    }

    public ArrayList<Date> getAsDates() {
        ArrayList<Date> dates = new ArrayList<>(unixTimestamps.length);
        for (long unixTimestamp : unixTimestamps) {
            dates.add(new Date(unixTimestamp * 1000));
        }
        return dates;
    }
}