package io.banditoz.mchelper.urbandictionary;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UDResult {
    @JsonProperty("list")
    private List<UDDefinition> results;

    public List<UDDefinition> getResults() {
        return results;
    }
}