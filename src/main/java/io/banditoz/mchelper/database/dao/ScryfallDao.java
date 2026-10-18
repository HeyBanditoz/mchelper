package io.banditoz.mchelper.database.dao;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import io.banditoz.mchelper.database.ScryfallCardRow;
import io.banditoz.mchelper.database.ScryfallPrinting;
import io.banditoz.mchelper.mtg.ScryfallCard;

public interface ScryfallDao {
    /**
     * Deletes all cards then re-imports the passed in ones.
     *
     * @param rows The cards to import. Consumed lazily.
     * @return The number of rows imported.
     */
    int replaceAll(Iterator<ScryfallCardRow> rows) throws SQLException;

    /**
     * @param search The name as the user typed it.
     * @return The best matching card, as its most regular recent English printing, or empty if nothing is close.
     */
    Optional<ScryfallCard> findByFuzzyName(String search) throws SQLException;

    /**
     * Suggests card names for a partially typed one, matching from the start of the name or the start of any word
     * within it, so <code>bolt</code> finds <code>Lightning Bolt</code>.
     *
     * @param search The partial name as the user typed it.
     * @param limit  Most names to return.
     * @return Matching names, most popular first, distinct and never more than <code>limit</code>.
     */
    List<String> findNamesStartingWith(String search, int limit) throws SQLException;

    /**
     * Lists the English printings of a card, newest first.
     *
     * @param printingId Scryfall id of any one printing of the card.
     * @param filter     Space separated words, each of which has to match the start of the set code or collector
     *                   number, or appear in the set name. Empty lists every printing.
     * @param limit      Most printings to return.
     */
    List<ScryfallPrinting> findPrintings(String printingId, String filter, int limit) throws SQLException;

    /**
     * Finds one specific printing of a card.
     *
     * @param printingId      Scryfall id of any one printing of the card.
     * @param setCode         Set code of the wanted printing, case insensitive.
     * @param collectorNumber Collector number of the wanted printing, case insensitive. Empty picks the most regular
     *                        printing in the set, same as {@link #findByFuzzyName(String)} would across all sets.
     * @return The printing, or empty if the card has none matching.
     */
    Optional<ScryfallCard> findPrinting(String printingId, String setCode, String collectorNumber) throws SQLException;

    /** @return When the most recent import happened, or empty if nothing has been imported yet. */
    Optional<OffsetDateTime> lastImportedAt() throws SQLException;
}
