package io.banditoz.mchelper.utils.quotes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Quote {
    @JsonProperty("contents")
    private Contents contents;

    public void setContents(Contents contents) {
        this.contents = contents;
    }

    public Contents getContents() {
        return contents;
    }
}