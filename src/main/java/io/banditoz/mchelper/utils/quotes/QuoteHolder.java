package io.banditoz.mchelper.utils.quotes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuoteHolder {
    @JsonProperty("quote")
    private Quote quote;

    public Quote getQuote() {
        return quote;
    }
}