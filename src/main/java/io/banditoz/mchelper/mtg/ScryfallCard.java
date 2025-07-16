package io.banditoz.mchelper.mtg;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.avaje.recordbuilder.RecordBuilder;

/**
 * A Magic: the Gathering trading card, mostly represented how the
 * <a href="https://scryfall.com/docs/api">Scryfall API</a> lays it out.
 *
 * @param id              Scryfall's identifier for the card.
 * @param faces           A list of card faces. Generally a single-element list.
 * @param name            The name of the card. If multiple faces, should be concatenated like `Alive // Well`
 * @param scryfallUri     The URL to Scryfall.
 * @param imageUris       Map of image URLs. You most likely want `png` from this map. Could be empty if each face has
 *                        its own
 * @param legalities      A map of legalities. Appears to be `not_legal` or `legal` TODO enum?
 * @param set             The set the card comes from. Example: FIN
 * @param setName         Full name of the set.
 * @param collectorNumber Card number.
 * @param releasedAt      Date the card was released.
 * @param flavorText      Flavor text, if present on the card.
 * @param scryfallSetUri  URL of the set.
 * @apiNote If you need to add fields to this class, you will also need to update the {@link ScryfallCardDeserializer} class
 * as that is where this object is built.
 */
@JsonDeserialize(using = ScryfallCardDeserializer.class)
@RecordBuilder
public record ScryfallCard(String id, List<ScryfallCardFace> faces, String name, String scryfallUri,
                           ScryfallImage imageUris, Map<String, String> legalities, String set, String setName,
                           String collectorNumber, LocalDate releasedAt, String flavorText, String scryfallSetUri) {
}
