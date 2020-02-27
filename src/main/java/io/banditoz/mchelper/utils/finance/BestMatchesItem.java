package io.banditoz.mchelper.utils.finance;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class BestMatchesItem {
    @JsonProperty("1. symbol")
    private String symbol;

    @JsonProperty("2. name")
    private String name;

    @JsonProperty("3. type")
    private String type;

    @JsonProperty("4. region")
    private String region;

    @JsonProperty("5. marketOpen")
    private String marketOpen; // TODO Use date or time instead, but only if we actually start using this datatype.

    @JsonProperty("6. marketClose")
    private String marketClose;

    @JsonProperty("7. timezone")
    private String timezone;

    @JsonProperty("8. currency")
    private String currency;

    @JsonProperty("9. matchScore")
    private BigDecimal matchScore;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setMatchScore(BigDecimal matchScore) {
        this.matchScore = matchScore;
    }

    public BigDecimal getMatchScore() {
        return matchScore;
    }

    public void setMarketOpen(String marketOpen) {
        this.marketOpen = marketOpen;
    }

    public String getMarketOpen() {
        return marketOpen;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRegion() {
        return region;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setMarketClose(String marketClose) {
        this.marketClose = marketClose;
    }

    public String getMarketClose() {
        return marketClose;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}