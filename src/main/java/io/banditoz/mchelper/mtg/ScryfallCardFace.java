package io.banditoz.mchelper.mtg;

import io.avaje.recordbuilder.RecordBuilder;

@RecordBuilder
public record ScryfallCardFace(String name, String manaCost, String typeLine, String oracleText,
                               String power, String toughness, String flavorText, ScryfallImage imageUris) {
}
