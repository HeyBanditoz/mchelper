package io.banditoz.mchelper.mtg;

import java.io.IOException;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class ScryfallCardDeserializer extends JsonDeserializer<ScryfallCard> {
    @Override
    public ScryfallCard deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        JsonNode cardFaces = node.get("card_faces");
        ScryfallCardBuilder card = ScryfallCardBuilder.builder();
        card.id(node.path("id").asText(null));
        card.name(node.path("name").asText(null));
        card.scryfallUri(node.path("scryfall_uri").asText(null));
        card.releasedAt(LocalDate.parse(node.path("released_at").asText(null)));
        card.set(node.path("set").asText(null));
        card.setName(node.path("set_name").asText(null));
        card.scryfallSetUri(node.path("scryfall_uri").asText(null));
        card.collectorNumber(node.path("collector_number").asText(null));
        JsonParser imageUris = node.path("image_uris").traverse();
        if (imageUris.nextToken() != JsonToken.NOT_AVAILABLE) {
            card.imageUris(ctxt.readValue(imageUris, ScryfallImage.class));
        }
        if (cardFaces == null) {
            ScryfallCardFaceBuilder cardFace = ScryfallCardFaceBuilder.builder();
            cardFace.name(node.path("name").asText(null));
            cardFace.manaCost(node.path("mana_cost").asText(null));
            cardFace.typeLine(node.path("type_line").asText(null));
            cardFace.oracleText(node.path("oracle_text").asText(null));
            cardFace.power(node.path("power").asText(null));
            cardFace.toughness(node.path("toughness").asText(null));
            cardFace.flavorText(node.path("flavor_text").asText(null));
            card.addFaces(cardFace.build());
        }
        else {
            for (JsonNode faceJson : node.path("card_faces")) {
                ScryfallCardFaceBuilder cardFace = ScryfallCardFaceBuilder.builder();
                cardFace.name(faceJson.path("name").asText(null));
                cardFace.manaCost(faceJson.path("mana_cost").asText(null));
                cardFace.typeLine(faceJson.path("type_line").asText(null));
                cardFace.oracleText(faceJson.path("oracle_text").asText(null));
                cardFace.power(faceJson.path("power").asText(null));
                cardFace.toughness(faceJson.path("toughness").asText(null));
                cardFace.flavorText(faceJson.path("flavor_text").asText(null));
                JsonParser faceImageUris = faceJson.path("image_uris").traverse();
                if (faceImageUris.nextToken() != JsonToken.NOT_AVAILABLE) {
                    cardFace.imageUris(ctxt.readValue(faceImageUris, ScryfallImage.class));
                }
                card.addFaces(cardFace.build());
            }
        }
        return card.build();
    }
}
