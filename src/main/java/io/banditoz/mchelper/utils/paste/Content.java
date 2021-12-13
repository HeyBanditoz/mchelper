package io.banditoz.mchelper.utils.paste;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Content {
    @JsonProperty("format")
    private String format;
    @JsonProperty("highlight_language")
    private Object highlightLanguage;
    @JsonProperty("value")
    private String value;
    @JsonProperty("content")
    private String content;

    @JsonProperty("format")
    public String getFormat() {
        return format;
    }

    @JsonProperty("format")
    public void setFormat(String format) {
        this.format = format;
    }

    @JsonProperty("highlight_language")
    public Object getHighlightLanguage() {
        return highlightLanguage;
    }

    @JsonProperty("highlight_language")
    public void setHighlightLanguage(Object highlightLanguage) {
        this.highlightLanguage = highlightLanguage;
    }

    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(String value) {
        this.value = value;
    }

    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    @JsonProperty("content")
    public void setContent(String content) {
        this.content = content;
    }
}
