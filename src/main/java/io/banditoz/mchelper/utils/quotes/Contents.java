package io.banditoz.mchelper.utils.quotes;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Contents {
    @JsonProperty("quotes")
    private List<Quote> quotes;

    public void setQuotes(List<Quote> quotes) {
        this.quotes = quotes;
    }

    public List<Quote> getQuotes() {
        return quotes;
    }
}