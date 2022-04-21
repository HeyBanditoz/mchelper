package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.NamedQuote;
import io.banditoz.mchelper.utils.database.StatPoint;
import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Query;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
            return Query.of("INSERT INTO quotes (guild_id, author_id, quote, quote_author) VALUES (:g, :a, :q, :qa) RETURNING id")
                    .on(
                            Param.value("g", nq.getGuildId()),
                            Param.value("a", nq.getAuthorId()),
                            Param.value("q", nq.getQuote()),
                            Param.value("qa", nq.getQuoteAuthor()))
                    .as((rs, ignored) -> {
                        rs.next();
                        return rs.getInt(1);
                    }, c);
        }
    }

    @Override
    public List<NamedQuote> getQuotesByMatch(String search, @NotNull Guild g) throws SQLException {
        search = '%' + search + '%';
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT * FROM quotes WHERE guild_id=:g AND (quote ILIKE :s OR quote_author ILIKE :t)")
                    .on(
                            Param.value("g", g.getIdLong()),
                            Param.value("s", search),
                            Param.value("t", search))
                    .as(this::parseMany, c);
        }
    }

    @Override
    public List<NamedQuote> getAllQuotesForGuild(@NotNull Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT * FROM quotes WHERE guild_id=:g ORDER BY RANDOM()")
                    .on(Param.value("g", g.getIdLong()))
                    .as(this::parseMany, c);
        }
    }

    @Override
    public @Nullable NamedQuote getRandomQuote(Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT * FROM quotes WHERE guild_id=:g ORDER BY RANDOM() LIMIT 1")
                    .on(Param.value("g", g.getIdLong()))
                    .as(this::parseOne, c);
        }
    }

    @Override
    public List<StatPoint<Long, Integer>> getUniqueAuthorQuoteCountPerGuild(Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT author_id, COUNT(author_id) AS \"count\" FROM quotes WHERE guild_id=:g GROUP BY author_id ORDER BY COUNT(author_id) DESC")
                    .on(Param.value("g", g.getIdLong()))
                    .as((rs, conn) -> {
                        List<StatPoint<Long, Integer>> stats = new ArrayList<>();
                        while (rs.next()) {
                            stats.add(new StatPoint<>(rs.getLong("author_id"), rs.getInt("count")));
                        }
                        return stats;
                    }, c);
        }
    }

    @Override
    public void editQuote(int id, NamedQuote nq) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            Query.of("UPDATE quotes SET quote=:q, quote_author=:a WHERE id=:i")
                    .on(
                            Param.value("q", nq.getQuote()),
                            Param.value("a", nq.getQuoteAuthor()),
                            Param.value("i", id)
                    ).executeUpdate(c);
        }
    }

    @Override
    public boolean deleteQuote(int id, Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            NamedQuote nq = Query.of("SELECT * FROM quotes WHERE id=:i AND guild_id=:g")
                    .on(
                            Param.value("i", id),
                            Param.value("g", g.getIdLong())
                    ).as(this::parseOne, c);
            if (nq == null) {
                return false;
            }
            LOGGER.info("Deleting quote #" + nq.getId() + " from guild \"" + g.getName() + "\" with content: " + nq.formatPlain());
            return Query.of("DELETE FROM quotes WHERE id=:i")
                    .on(Param.value("i", id))
                    .executeUpdate(c) <= 1;
        }
    }

    private @Nullable NamedQuote parseOne(ResultSet rs, Connection c) throws SQLException {
        if (!rs.next()) {
            return null;
        }
        NamedQuote nq = new NamedQuote();
        nq.setGuildId(rs.getLong("guild_id"));
        nq.setAuthorId(rs.getLong("author_id"));
        nq.setQuote(rs.getString("quote"));
        nq.setQuoteAuthor(rs.getString("quote_author"));
        nq.setLastModified(rs.getTimestamp("last_modified"));
        nq.setId(rs.getInt("id"));
        return nq;
    }

    private List<NamedQuote> parseMany(ResultSet rs, Connection c) throws SQLException {
        List<NamedQuote> quotes = new ArrayList<>();
        while (!rs.isLast()) {
            NamedQuote nq = parseOne(rs, c);
            if (nq != null) {
                quotes.add(nq);
            }
        }
        return quotes;
    }
}
