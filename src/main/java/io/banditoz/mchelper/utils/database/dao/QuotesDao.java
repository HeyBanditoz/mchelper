package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.NamedQuote;
import io.banditoz.mchelper.utils.database.StatPoint;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface QuotesDao {
    /**
     * Saves a {@link NamedQuote} to the database.
     *
     * @param nq The Quote to save.
     * @return The id of the quote added.
     * @throws SQLException If there was an error saving a quote.
     */
    int saveQuote(NamedQuote nq) throws SQLException;
    /**
     * Returns a list of quotes by a {@link Guild} by a search.
     *
     * @param search The {@link String} to search for in the database.
     * @param g      The {@link Guild} to search by.
     * @return A {@link List} that may or may not contain {@link NamedQuote}s for the guild.
     * @throws SQLException If there was an error getting the quote.
     */
    List<NamedQuote> getQuotesByMatch(String search, Guild g) throws SQLException;
    /**
     * Returns a list of all quotes by a {@link Guild}.
     *
     * @param g The {@link Guild} to search for in the database.
     * @return A {@link List} that may or may not contain {@link NamedQuote}s for the guild.
     * @throws SQLException If there was an error getting the quote.
     */
    List<NamedQuote> getAllQuotesForGuild(Guild g) throws SQLException;
    /**
     * Returns a random quote by a {@link Guild} A random one will be retrieved.
     *
     * @param g The {@link Guild} to search by.
     * @return An {@link Optional} that may or may not contain a {@link NamedQuote}.
     * @throws SQLException If there was an error getting the quote.
     */
    @Nullable
    NamedQuote getRandomQuote(Guild g) throws SQLException;

    /**
     * Returns a list of all quotes by a certain {@link net.dv8tion.jda.api.entities.User} in a {@link Guild}.
     *
     * @param u The {@link net.dv8tion.jda.api.entities.User} ID to search by
     * @param g The {@link Guild} to search in.
     * @return A {@link List} that may or may not contain {@link NamedQuote}s for the guild.
     * @throws SQLException If there was an error getting the quote.
     */
    List<NamedQuote> getAllQuotesByAuthorInGuild(long u, Guild g) throws SQLException;

    /**
     * Returns a {@link Map} containing a list of keys of author IDs, that match to values on how many quotes that
     * author has added. Ideally, this {@link Map} should be ordered, as the command that invokes it expects it to be.
     *
     * @param g The {@link Guild} to filter by.
     * @return A {@link Map} of how many quotes a unique author has added, empty if the guild contains no quotes.
     * @throws SQLException If there was an error getting the quotes.
     */
    List<StatPoint<Long>> getUniqueAuthorQuoteCountPerGuild(Guild g) throws SQLException;

    /**
     * Saves a {@link NamedQuote} in the database.
     *
     * @param id The id of the Quote to edit.
     * @throws SQLException If there was an error saving a quote.
     */
    void editQuote(int id, NamedQuote nq) throws SQLException;

    /**
     * Attempts to delete a quote given an ID and the guild it should be from.
     *
     * @param id The ID of the quote.
     * @param g The guild it's in.
     * @return Whether or not the quote deletion succeeded.
     * @throws SQLException If there was an error deleting the quote.
     */
    boolean deleteQuote(int id, Guild g) throws SQLException;
}
