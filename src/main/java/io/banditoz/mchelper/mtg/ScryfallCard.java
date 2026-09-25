package io.banditoz.mchelper.mtg;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.avaje.recordbuilder.RecordBuilder;

/**
 * A Magic: the Gathering trading card, mostly represented how the
 * <a href="https://scryfall.com/docs/api">Scryfall API</a> lays it out.
 *
 * @param id              Scryfall's identifier for the card.
 * @param scryfallUri     The URL to Scryfall.
 * @param imageUris       Map of image URLs. You most likely want `png` from this map. Could be null if each face has
 *                        its own
 * @param legalities      A map of legalities. Appears to be `not_legal` or `legal` TODO enum?
 * @param set             The set the card comes from. Example: FIN
 * @param setName         Full name of the set.
 * @param collectorNumber Card number.
 * @param releasedAt      Date the card was released.
 * @param scryfallSetUri  URL of the set.
 * @param cardFaces       The card's faces, if it has more than one. Null for single-faced cards. You most likely want
 *                        {@link #faces()} instead.
 * @param topLevelFace    Single-faced cards put their face fields (mana cost, oracle text, etc.) at the top level of
 *                        the card object, so they're collected here. You most likely want {@link #faces()} instead.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@RecordBuilder
public record ScryfallCard(String id, String scryfallUri, ScryfallImage imageUris,
                           Map<String, String> legalities, String set, String setName, String collectorNumber,
                           LocalDate releasedAt, String scryfallSetUri, List<ScryfallCardFace> cardFaces,
                           @JsonUnwrapped ScryfallCardFace topLevelFace) {
    /** @return A list of card faces. Generally a single-element list. */
    public List<ScryfallCardFace> faces() {
        return cardFaces != null ? cardFaces : List.of(topLevelFace);
    }

    /** @return The name of the card. If multiple faces, concatenated like `Alive // Well` */
    public String name() {
        return faces().stream().map(ScryfallCardFace::name).collect(Collectors.joining(" // "));
    }
}
