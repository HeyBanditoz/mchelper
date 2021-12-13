package io.banditoz.mchelper.utils.paste;

import com.fasterxml.jackson.annotation.JsonProperty;

public class File {
    @JsonProperty("name")
    private String name;
    @JsonProperty("content")
    private Content content;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("content")
    public Content getContent() {
        return content;
    }

    @JsonProperty("content")
    public void setContent(Content content) {
        this.content = content;
    }
}
