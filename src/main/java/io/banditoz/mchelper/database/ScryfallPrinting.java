package io.banditoz.mchelper.database;

import java.time.LocalDate;

/**
 * Enough about one printing of a card to tell it apart from the card's other printings.
 *
 * @param setCode         Set code, e.g. <code>sld</code>.
 * @param setName         Full name of the set, e.g. <code>Secret Lair Drop</code>.
 * @param collectorNumber Collector number within the set, e.g. <code>1512</code> or <code>1494★</code>.
 * @param releasedAt      Date this printing was released.
 */
public record ScryfallPrinting(String setCode, String setName, String collectorNumber, LocalDate releasedAt) {
}
