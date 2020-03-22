package io.banditoz.mchelper.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NamedQuote {
    @JsonProperty("name")
    private String name;
    @JsonProperty("quote")
    private String quote;

    public NamedQuote(String name, String quote) {
        this.name = name;
        this.quote = quote;
    }

    public NamedQuote() {}

    public String buildResponse() {
        return "“" + this.getQuote() + "” --" + this.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }
}
