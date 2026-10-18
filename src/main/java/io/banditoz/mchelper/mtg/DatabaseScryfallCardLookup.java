package io.banditoz.mchelper.mtg;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import io.banditoz.mchelper.database.ScryfallPrinting;
import io.banditoz.mchelper.database.dao.ScryfallDao;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresDatabase
public class DatabaseScryfallCardLookup {
    private static final Logger log = LoggerFactory.getLogger(DatabaseScryfallCardLookup.class);
    private final ScryfallDao scryfallDao;

    @Inject
    public DatabaseScryfallCardLookup(ScryfallDao scryfallDao) {
        this.scryfallDao = scryfallDao;
    }

    /**
     * Finds the card best matching a name, tolerating typos and partial names.
     *
     * @param search The name as the user typed it.
     * @return The best matching card, or empty if nothing matches closely enough or the database can't be queried.
     */
    public Optional<ScryfallCard> findByFuzzyName(String search) {
        try {
            return scryfallDao.findByFuzzyName(search);
        } catch (SQLException ex) {
            log.warn("\"Couldn't look up card in the database.\" name=\"{}\"", search, ex);
            return Optional.empty();
        }
    }

    /**
     * Suggests card names for a partially typed one.
     *
     * @param search The partial name as the user typed it.
     * @param limit  Most names to return.
     * @return Matching names, most popular first, or empty if nothing matches or the database can't be queried.
     */
    public List<String> findNamesStartingWith(String search, int limit) {
        try {
            return scryfallDao.findNamesStartingWith(search, limit);
        } catch (SQLException ex) {
            log.warn("\"Couldn't suggest card names.\" name=\"{}\"", search, ex);
            return List.of();
        }
    }

    /**
     * Lists the English printings of a card, newest first.
     *
     * @param printingId Scryfall id of any one printing of the card.
     * @param filter     Space separated words narrowing down the printings, see
     *                   {@link ScryfallDao#findPrintings(String, String, int)}. Empty lists every printing.
     * @param limit      Most printings to return.
     * @return The printings, or empty if none match or the database can't be queried.
     */
    public List<ScryfallPrinting> findPrintings(String printingId, String filter, int limit) {
        try {
            return scryfallDao.findPrintings(printingId, filter, limit);
        } catch (SQLException ex) {
            log.warn("\"Couldn't list card printings.\" id=\"{}\" filter=\"{}\"", printingId, filter, ex);
            return List.of();
        }
    }

    /**
     * Finds one specific printing of a card.
     *
     * @param printingId      Scryfall id of any one printing of the card.
     * @param setCode         Set code of the wanted printing, case insensitive.
     * @param collectorNumber Collector number of the wanted printing, case insensitive. Empty picks the most regular
     *                        printing in the set.
     * @return The printing, or empty if the card has none matching or the database can't be queried.
     */
    public Optional<ScryfallCard> findPrinting(String printingId, String setCode, String collectorNumber) {
        try {
            return scryfallDao.findPrinting(printingId, setCode, collectorNumber);
        } catch (SQLException ex) {
            log.warn("\"Couldn't look up card printing.\" id=\"{}\" set=\"{}\" number=\"{}\"",
                    printingId, setCode, collectorNumber, ex);
            return Optional.empty();
        }
    }
}
