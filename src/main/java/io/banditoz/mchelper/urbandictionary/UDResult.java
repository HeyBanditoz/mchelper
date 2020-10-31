package io.banditoz.mchelper.urbandictionary;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UDResult {
    @JsonProperty("list")
    private List<UDDefinition> results;

    public List<UDDefinition> getResults() {
        return results;
    }
}