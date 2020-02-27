package io.banditoz.mchelper.utils.finance;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TickerMatches {
    @JsonProperty("bestMatches")
    private List<BestMatchesItem> bestMatches;

    public void setBestMatches(List<BestMatchesItem> bestMatches) {
        this.bestMatches = bestMatches;
    }

    public List<BestMatchesItem> getBestMatches() {
        return bestMatches;
    }
}