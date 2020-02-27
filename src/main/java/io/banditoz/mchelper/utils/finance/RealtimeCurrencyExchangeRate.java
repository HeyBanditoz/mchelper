package io.banditoz.mchelper.utils.finance;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.banditoz.mchelper.utils.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RealtimeCurrencyExchangeRate {
    @JsonProperty("1. From_Currency Code")
    private String fromCurrencyCode;

    @JsonProperty("2. From_Currency Name")
    private String fromCurrencyName;

    @JsonProperty("3. To_Currency Code")
    private String toCurrencyCode;

    @JsonProperty("4. To_Currency Name")
    private String toCurrencyName;

    @JsonProperty("5. Exchange Rate")
    @JsonDeserialize(using = BigDecimalMonetaryDeserializer.class)
    private BigDecimal exchangeRate;

    @JsonProperty("6. Last Refreshed")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastRefreshed;

    @JsonProperty("7. Time Zone")
    private String timeZone;

    @JsonProperty("8. Bid Price")
    @JsonDeserialize(using = BigDecimalMonetaryDeserializer.class)
    private BigDecimal bidPrice;

    @JsonProperty("9. Ask Price")
    @JsonDeserialize(using = BigDecimalMonetaryDeserializer.class)
    private BigDecimal askPrice;

    public String getPrettyDateTime() {
        return DateUtils.getLocallyZonedRFC1123(getLastRefreshed());
    }

    public String getFromCurrencyCode() {
        return fromCurrencyCode;
    }

    public void setFromCurrencyCode(String fromCurrencyCode) {
        this.fromCurrencyCode = fromCurrencyCode;
    }

    public String getFromCurrencyName() {
        return fromCurrencyName;
    }

    public void setFromCurrencyName(String fromCurrencyName) {
        this.fromCurrencyName = fromCurrencyName;
    }

    public String getToCurrencyCode() {
        return toCurrencyCode;
    }

    public void setToCurrencyCode(String toCurrencyCode) {
        this.toCurrencyCode = toCurrencyCode;
    }

    public String getToCurrencyName() {
        return toCurrencyName;
    }

    public void setToCurrencyName(String toCurrencyName) {
        this.toCurrencyName = toCurrencyName;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public LocalDateTime getLastRefreshed() {
        return lastRefreshed;
    }

    public void setLastRefreshed(LocalDateTime lastRefreshed) {
        this.lastRefreshed = lastRefreshed;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public BigDecimal getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(BigDecimal bidPrice) {
        this.bidPrice = bidPrice;
    }

    public BigDecimal getAskPrice() {
        return askPrice;
    }

    public void setAskPrice(BigDecimal askPrice) {
        this.askPrice = askPrice;
    }

    @Override
    public String toString() {
        return "RealtimeCurrencyExchangeRate{" +
                "fromCurrencyCode='" + fromCurrencyCode + '\'' +
                ", fromCurrencyName='" + fromCurrencyName + '\'' +
                ", toCurrencyCode='" + toCurrencyCode + '\'' +
                ", toCurrencyName='" + toCurrencyName + '\'' +
                ", exchangeRate=" + exchangeRate +
                ", lastRefreshed='" + lastRefreshed + '\'' +
                ", timeZone='" + timeZone + '\'' +
                ", bidPrice=" + bidPrice +
                ", askPrice=" + askPrice +
                '}';
    }
}
