package io.banditoz.mchelper.database;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * One printing of a Magic card as stored in the <code>mtg_cards</code> table.
 *
 * @param id              Scryfall's identifier for this printing.
 * @param oracleId        Shared by every printing of the same card.
 * @param name            Card name, multiple faces joined like <code>Alive // Well</code>.
 * @param searchName      Lowercased name, used for fuzzy matching.
 * @param lang            Printed language, e.g. <code>en</code>.
 * @param setCode         Set code, e.g. <code>blb</code>.
 * @param collectorNumber Collector number within the set.
 * @param releasedAt      Date this printing was released.
 * @param layout          Scryfall layout, e.g. <code>normal</code> or <code>transform</code>.
 * @param normalPrint     If this is a regular printing, rather than a promo, special frame, reprint list, etc.
 * @param edhrecRank      EDHREC popularity rank, lower is more popular. Null if unranked.
 * @param data            Full Scryfall card JSON, deserializable into {@link io.banditoz.mchelper.mtg.ScryfallCard}.
 */
public record ScryfallCardRow(UUID id, UUID oracleId, String name, String searchName, String lang, String setCode,
                              String collectorNumber, LocalDate releasedAt, String layout, boolean normalPrint,
                              @Nullable Integer edhrecRank, String data) {
}
