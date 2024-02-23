package io.banditoz.mchelper.investing.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyProfile {
    @JsonProperty("finnhubIndustry")
    private String finnhubIndustry;

    @JsonProperty("country")
    private String country;

    @JsonProperty("ticker")
    private String ticker;

    @JsonProperty("marketCapitalization")
    private float marketCapitalization;

    @JsonProperty("weburl")
    private String weburl;

    @JsonProperty("name")
    private String name;

    @JsonProperty("ipo")
    private Date ipo;

    @JsonProperty("logo")
    private String logo;

    @JsonProperty("exchange")
    private String exchange;

    @JsonProperty("shareOutstanding")
    private double shareOutstanding;

    private Timestamp addedWhen;

    public String getFinnhubIndustry() {
        return finnhubIndustry;
    }

    public String getCountry() {
        return country;
    }

    public String getTicker() {
        return ticker;
    }

    public float getMarketCapitalization() {
        return marketCapitalization;
    }

    public String getWeburl() {
        return weburl;
    }

    public String getName() {
        return name;
    }

    public Date getIpo() {
        return ipo;
    }

    public String getLogo() {
        return logo;
    }

    public String getExchange() {
        return exchange;
    }

    public double getShareOutstanding() {
        return shareOutstanding;
    }

    public void setFinnhubIndustry(String finnhubIndustry) {
        this.finnhubIndustry = finnhubIndustry;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public void setMarketCapitalization(float marketCapitalization) {
        this.marketCapitalization = marketCapitalization;
    }

    public void setWeburl(String weburl) {
        this.weburl = weburl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIpo(Date ipo) {
        this.ipo = ipo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public void setShareOutstanding(double shareOutstanding) {
        this.shareOutstanding = shareOutstanding;
    }

    public void setAddedWhen(Timestamp addedWhen) {
        this.addedWhen = addedWhen;
    }

    /**
     * Checks if this CompanyProfile is expired. It will be cached in the database for thirty days.
     * @return true if this CompanyProfile from the database if expired, false if not, or if addedWhen is null.
     */
    public boolean isExpired() {
        if (addedWhen == null) {
            return false;
        }
        return addedWhen.toInstant().plus(30, ChronoUnit.DAYS).isBefore(Instant.now());
    }
}