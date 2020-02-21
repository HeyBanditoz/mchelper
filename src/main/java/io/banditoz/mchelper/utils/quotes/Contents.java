package io.banditoz.mchelper.utils.quotes;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Contents {
    @JsonProperty("quotes")
    private List<QuoteItem> quotes;

    public void setQuotes(List<QuoteItem> quotes) {
        this.quotes = quotes;
    }

    public List<QuoteItem> getQuotes() {
        return quotes;
    }
}