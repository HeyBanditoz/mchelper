package io.banditoz.mchelper.mtg;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ScryfallCard(String name, String scryfallUri, String manaCost, String typeLine, String oracleText,
                           ScryfallImage imageUris, Map<String, String> legalities, String set, String setName,
                           String collectorNumber, String releasedAt, String flavorText, String scryfallSetUri,
                           String power, String toughness) {
}
