package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.NamedQuote;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.SQLException;
import java.util.Optional;

public interface QuotesDao {
    /**
     * Saves a {@link NamedQuote} to the database.
     * @param nq The Quote to save.
     * @throws SQLException If there was an error saving a quote.
     */
    void saveQuote(NamedQuote nq) throws SQLException;
    /**
     * Returns a random quote by a {@link Guild} if the quote contains a String. If multiple are found, a random one
     * will be retrieved.
     *
     * @param search The {@link String} to search for in the database.
     * @param g The {@link Guild} to search by.
     * @return An {@link Optional} that may or may not contain a {@link NamedQuote}.
     * @throws SQLException If there was an error getting the quote.
     */
    Optional<NamedQuote> getRandomQuoteByMatch(String search, Guild g) throws SQLException;
    /**
     * Returns a random quote by a {@link Guild} A random one will be retrieved.
     *
     * @param g The {@link Guild} to search by.
     * @return An {@link Optional} that may or may not contain a {@link NamedQuote}.
     * @throws SQLException If there was an error getting the quote.
     */
    Optional<NamedQuote> getRandomQuote(Guild g) throws SQLException;
}
