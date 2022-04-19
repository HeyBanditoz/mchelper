package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.NamedQuote;
import io.banditoz.mchelper.utils.database.StatPoint;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class QuotesDaoImpl extends Dao implements QuotesDao {
    public QuotesDaoImpl(Database database) {
        super(database);
    }

    @Override
    public String getSqlTableGenerator() {
        return """
                CREATE TABLE IF NOT EXISTS quotes (
                    guild_id bigint NOT NULL,
                    author_id bigint NOT NULL,
                    quote character varying(1500) NOT NULL,
                    quote_author character varying(100) NOT NULL,
                    last_modified timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    id SERIAL,
                    PRIMARY KEY (id),
                    UNIQUE (guild_id, quote, quote_author)
                );
                """;
    }

    @Override
    public int saveQuote(NamedQuote nq) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO quotes (guild_id, author_id, quote, quote_author) VALUES (?, ?, ?, ?) RETURNING id");
            ps.setLong(1, nq.getGuildId());
            ps.setLong(2, nq.getAuthorId());
            ps.setString(3, nq.getQuote());
            ps.setString(4, nq.getQuoteAuthor());
            ResultSet rs = ps.executeQuery();
            rs.next();
            int id = rs.getInt("id");
            ps.close();
            rs.close();
            return id;
        }
    }

    @Override
    public List<NamedQuote> getQuotesByMatch(String search, @NotNull Guild g) throws SQLException {
        search = "%" + search + "%";
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM quotes WHERE guild_id=? AND (quote ILIKE ? OR quote_author ILIKE ?) ORDER BY RANDOM()");
            ps.setLong(1, g.getIdLong());
            ps.setString(2, search);
            ps.setString(3, search);
            return buildNamedQuotesFromResultSet(ps);
        }
    }

    @Override
    public List<NamedQuote> getAllQuotesForGuild(@NotNull Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM quotes WHERE guild_id=? ORDER BY RANDOM()");
            ps.setLong(1, g.getIdLong());
            return buildNamedQuotesFromResultSet(ps);
        }
    }


    @Override
    public Optional<NamedQuote> getRandomQuote(Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM quotes WHERE guild_id=? ORDER BY RANDOM() LIMIT 1");
            ps.setLong(1, g.getIdLong());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return buildQuoteFromResultSet(rs);
            }
        }
    }

    @Override
    public List<StatPoint<Long, Integer>> getUniqueAuthorQuoteCountPerGuild(Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT author_id, COUNT(author_id) AS \"count\" FROM quotes WHERE guild_id=? GROUP BY author_id ORDER BY COUNT(author_id) DESC");
            ps.setLong(1, g.getIdLong());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.isLast()) {
                    ArrayList<StatPoint<Long, Integer>> stats = new ArrayList<>();
                    while (rs.next()) {
                        stats.add(new StatPoint<>(rs.getLong("author_id"), rs.getInt("count")));
                    }
                    return stats;
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void editQuote(int id, NamedQuote nq) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("UPDATE quotes SET quote = ?, quote_author = ? WHERE id = ?");
            ps.setString(1, nq.getQuote());
            ps.setString(2, nq.getQuoteAuthor());
            ps.setInt(3, id);
            ps.execute();
            ps.close();
        }
    }

    @Override
    public boolean deleteQuote(int id, Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM quotes WHERE id=? AND guild_id=?");
            ps.setInt(1, id);
            ps.setLong(2, g.getIdLong());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                Optional<NamedQuote> nq = buildQuoteFromResultSet(rs);
                if (nq.isPresent()) {
                    NamedQuote quote = nq.get();
                    LOGGER.info("Deleting quote #" + quote.getId() + " from guild \"" + g.getName() + "\" with content: " + quote.formatPlain());
                    ps = c.prepareStatement("DELETE FROM quotes WHERE id=?");
                    ps.setInt(1, id);
                    ps.execute();
                    ps.close();
                    return true;
                }
                else {
                    ps.close();
                    return false;
                }
            }
        }
    }

    private Optional<NamedQuote> buildQuoteFromResultSet(ResultSet rs) throws SQLException {
        NamedQuote nq = new NamedQuote();
        nq.setGuildId(rs.getLong("guild_id"));
        nq.setAuthorId(rs.getLong("author_id"));
        nq.setQuote(rs.getString("quote"));
        nq.setQuoteAuthor(rs.getString("quote_author"));
        nq.setLastModified(rs.getTimestamp("last_modified"));
        nq.setId(rs.getInt("id"));
        return Optional.of(nq);
    }

    private List<NamedQuote> buildNamedQuotesFromResultSet(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            List<NamedQuote> quotes = new ArrayList<>();
            while (rs.next()) {
                Optional<NamedQuote> onq = buildQuoteFromResultSet(rs);
                onq.ifPresent(quotes::add);
            }
            return quotes;
        }
    }
}
