package io.banditoz.mchelper.utils.finance;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.math.BigDecimal;
import java.math.BigInteger;

public class GlobalQuote {
    @JsonProperty("Global Quote")
    private GlobalQuote globalQuote;

    @JsonProperty("01. symbol")
    private String symbol;

    @JsonProperty("02. open")
    private BigDecimal open;

    @JsonProperty("03. high")
    private BigDecimal high;

    @JsonProperty("04. low")
    private BigDecimal low;

    @JsonProperty("05. price")
    private BigDecimal price;

    @JsonProperty("06. volume")
    private BigInteger volume;

    @JsonProperty("07. latest trading day")
    private String latestTradingDay;

    @JsonProperty("08. previous close")
    private BigDecimal previousClose;

    @JsonProperty("09. change")
    private BigDecimal change;

    @JsonProperty("10. change percent")
    @JsonDeserialize(using = BigDecimalPercentDeserializer.class)
    private BigDecimal changePercent;

    public void setGlobalQuote(GlobalQuote globalQuote) {
        this.globalQuote = globalQuote;
    }

    public GlobalQuote getGlobalQuote() {
        return globalQuote;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void setChangePercent(BigDecimal changePercent) {
        this.changePercent = changePercent;
    }

    public BigDecimal getChangePercent() {
        return changePercent;
    }

    public void setLatestTradingDay(String latestTradingDay) {
        this.latestTradingDay = latestTradingDay;
    }

    public String getLatestTradingDay() {
        return latestTradingDay;
    }

    public void setPreviousClose(BigDecimal previousClose) {
        this.previousClose = previousClose;
    }

    public BigDecimal getPreviousClose() {
        return previousClose;
    }

    public void setOpen(BigDecimal open) {
        this.open = open;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public void setChange(BigDecimal change) {
        this.change = change;
    }

    public BigDecimal getChange() {
        return change;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setVolume(BigInteger volume) {
        this.volume = volume;
    }

    public BigInteger getVolume() {
        return volume;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public BigDecimal getHigh() {
        return high;
    }

    @Override
    public String toString() {
        return "GlobalQuote{" +
                "globalQuote=" + globalQuote +
                ", symbol='" + symbol + '\'' +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", price=" + price +
                ", volume=" + volume +
                ", latestTradingDay='" + latestTradingDay + '\'' +
                ", previousClose=" + previousClose +
                ", change=" + change +
                ", changePercent='" + changePercent + '\'' +
                '}';
    }
}