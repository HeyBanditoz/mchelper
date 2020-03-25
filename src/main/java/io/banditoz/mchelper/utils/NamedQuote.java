package io.banditoz.mchelper.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class NamedQuote {
    @JsonProperty("authorId")
    private String authorId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("quote")
    private String quote;

    public NamedQuote(String name, String quote, String authorId) {
        this.name = name;
        this.quote = quote;
        this.authorId = authorId;
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

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedQuote that = (NamedQuote) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(quote, that.quote) &&
                Objects.equals(authorId, that.authorId);
    }
}
