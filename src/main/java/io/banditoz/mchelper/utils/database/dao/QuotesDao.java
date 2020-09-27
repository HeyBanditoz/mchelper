package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.NamedQuote;
import io.banditoz.mchelper.utils.database.UserStat;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    /**
     * Returns a {@link Map} containing a list of keys of author IDs, that match to values on how many quotes that
     * author has added. Ideally, this {@link Map} should be ordered, as the command that invokes it expects it to be.
     *
     * @param g The {@link Guild} to filter by.
     * @return A {@link Map} of how many quotes a unique author has added, empty if the guild contains no quotes.
     * @throws SQLException If there was an error getting the quotes.
     */
    Set<UserStat> getUniqueAuthorQuoteCountPerGuild(Guild g) throws SQLException;
}
