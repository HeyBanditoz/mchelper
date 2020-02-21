package io.banditoz.mchelper.utils.finance;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Currency {
    @JsonProperty("Realtime Currency Exchange Rate")
    private RealtimeCurrencyExchangeRate realtimeCurrencyExchangeRate;

    public void setRealtimeCurrencyExchangeRate(RealtimeCurrencyExchangeRate realtimeCurrencyExchangeRate) {
        this.realtimeCurrencyExchangeRate = realtimeCurrencyExchangeRate;
    }

    public RealtimeCurrencyExchangeRate getRealtimeCurrencyExchangeRate() {
        return realtimeCurrencyExchangeRate;
    }
}