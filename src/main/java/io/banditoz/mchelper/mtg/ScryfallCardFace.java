package io.banditoz.mchelper.mtg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.avaje.recordbuilder.RecordBuilder;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@RecordBuilder
public record ScryfallCardFace(String name, String manaCost, String typeLine, String oracleText,
                               String power, String toughness, String flavorText, ScryfallImage imageUris) {
}
