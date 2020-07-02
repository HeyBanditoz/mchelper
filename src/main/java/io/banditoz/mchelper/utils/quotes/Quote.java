package io.banditoz.mchelper.utils.quotes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Quote {
    @JsonProperty("author")
    private String author;

    @JsonProperty("body")
    private String body;

    @JsonProperty("author")
    public String getAuthor() {
        return author;
    }

    @JsonProperty("body")
    public String getBody() {
        return body;
    }
}