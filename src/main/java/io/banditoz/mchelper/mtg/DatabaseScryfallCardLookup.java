package io.banditoz.mchelper.mtg;

import java.sql.SQLException;
import java.util.Optional;

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
}
