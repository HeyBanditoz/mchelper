package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.NamedQuote;
import io.banditoz.mchelper.utils.database.StatPoint;
import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Query;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static io.banditoz.mchelper.utils.database.NamedQuote.Flag;

public class QuotesDaoImpl extends Dao implements QuotesDao {
    public QuotesDaoImpl(Database database) {
        super(database);
    }

    @Override
    public int saveQuote(NamedQuote nq, EnumSet<Flag> flags) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            c.setAutoCommit(false);
            int qid = Query.of("INSERT INTO quotes (guild_id, author_id, quote, quote_author) VALUES (:g, :a, :q, :qa) RETURNING id")
                    .on(
                            Param.value("g", nq.getGuildId()),
                            Param.value("a", nq.getAuthorId()),
                            Param.value("q", nq.getQuote()),
                            Param.value("qa", nq.getQuoteAuthor()))
                    .as((rs, ignored) -> {
                        rs.next();
                        return rs.getInt(1);
                    }, c);
            // add all flags, if available, not batching as there probably won't be more than two
            for (Flag flag : flags) {
                Query.of("INSERT INTO quote_flags (quote_id, flag, created_by) VALUES (:q, :f, :b)")
                        .on(
                                Param.value("q", qid),
                                Param.value("f", flag.ordinal()),
                                Param.value("b", nq.getAuthorId())
                        )
                        .executeInsert(c);
            }
            c.commit();
            c.setAutoCommit(true);
            return qid;
        }
    }

    @Override
    public List<NamedQuote> getQuotesByMatch(String search, @NotNull Guild g) throws SQLException {
        search = '%' + search + '%';
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("""
                            SELECT *, (SELECT ARRAY_AGG(flag) FROM quote_flags qf WHERE qf.quote_id = id) AS flags
                            FROM quotes
                            WHERE guild_id = :g
                              AND (SELECT COUNT(*) FROM quote_flags qf WHERE qf.quote_id = id AND flag = :f) = 0
                              AND (quote ILIKE :s OR quote_author ILIKE :t)
                            ORDER BY RANDOM()""")
                    .on(
                            Param.value("g", g.getIdLong()),
                            Param.values("f", Flag.HIDDEN.ordinal()),
                            Param.value("s", search),
                            Param.value("t", search))
                    .as(this::parseManyQuotes, c);
        }
    }

    @Override
    public List<NamedQuote> getQuotesByFulltextSearch(String search, @NotNull Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("""
                            SELECT *, (SELECT ARRAY_AGG(flag) FROM quote_flags qf WHERE qf.quote_id = id) AS flags
                            FROM quotes
                            WHERE guild_id = :g
                              AND (SELECT COUNT(*) FROM quote_flags qf WHERE qf.quote_id = id AND flag = :f) = 0
                              AND ts @@ phraseto_tsquery('english', :s)
                            ORDER BY RANDOM()""")
                    .on(
                            Param.value("g", g.getIdLong()),
                            Param.values("f", Flag.HIDDEN.ordinal()),
                            Param.value("s", search)
                    )
                    .as(this::parseManyQuotes, c);
        }
    }

    @Override
    public List<NamedQuote> getAllQuotesForGuild(@NotNull Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("""
                            SELECT *, (SELECT ARRAY_AGG(flag) FROM quote_flags qf WHERE qf.quote_id = id) AS flags
                            FROM quotes
                            WHERE guild_id = :g
                              AND (SELECT COUNT(*) FROM quote_flags qf WHERE qf.quote_id = id AND flag = :f) = 0
                            ORDER BY RANDOM()""")
                    .on(
                            Param.value("g", g.getIdLong()),
                            Param.values("f", Flag.HIDDEN.ordinal())
                    )
                    .as(this::parseManyQuotes, c);
        }
    }

    @Override
    public @Nullable NamedQuote getRandomQuote(Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("""
                            SELECT *, (SELECT ARRAY_AGG(flag) FROM quote_flags qf WHERE qf.quote_id = id) AS flags
                            FROM quotes
                            WHERE guild_id = :g
                              AND (SELECT COUNT(*) FROM quote_flags qf WHERE qf.quote_id = id AND flag = :f) = 0
                            ORDER BY RANDOM()
                            LIMIT 1""")
                    .on(
                            Param.values("f", Flag.HIDDEN.ordinal()),
                            Param.value("g", g.getIdLong())
                    )
                    .as(this::parseOne, c);
        }
    }

    @Override
    public @Nullable NamedQuote getRandomQotd(Guild g, boolean onlyExcluded) throws SQLException {
        return onlyExcluded ? getRandomQotdQuoteExcludedQuotes(g) : getRandomQotdQuote(g);
    }

    private @Nullable NamedQuote getRandomQotdQuote(Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("""
                            SELECT *, (SELECT ARRAY_AGG(flag) FROM quote_flags qf WHERE qf.quote_id = id) AS flags
                            FROM quotes
                            WHERE guild_id = :g
                              AND (SELECT COUNT(*) FROM quote_flags qf WHERE qf.quote_id = id AND flag = :f) = 0
                              AND (SELECT COUNT(*) FROM quote_flags qf WHERE qf.quote_id = id AND flag = :ff) = 0
                            ORDER BY RANDOM()
                            LIMIT 1""")
                    .on(
                            Param.values("f", Flag.HIDDEN.ordinal()),
                            Param.value("ff", Flag.EXCLUDE_QOTD.ordinal()),
                            Param.value("g", g.getIdLong())
                    )
                    .as(this::parseOne, c);
        }
    }

    private @Nullable NamedQuote getRandomQotdQuoteExcludedQuotes(Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("""
                            WITH excluded_quotes AS (
                                SELECT id FROM quotes -- guild check can be skipped here, as this isn't the final result set
                                WHERE (SELECT COUNT(*) FROM quote_flags qf WHERE qf.quote_id = id AND flag = :ff) = 0
                            )
                            SELECT *, (SELECT ARRAY_AGG(flag) FROM quote_flags qf WHERE qf.quote_id = id) AS flags
                            FROM quotes
                            WHERE guild_id = :g
                              AND (SELECT COUNT(*) FROM quote_flags qf WHERE qf.quote_id = id AND flag = :f) = 0
                              AND id NOT IN (SELECT id FROM excluded_quotes)
                            ORDER BY RANDOM()
                            LIMIT 1""")
                    .on(
                            Param.values("f", Flag.HIDDEN.ordinal()),
                            Param.value("ff", Flag.EXCLUDE_QOTD.ordinal()),
                            Param.value("g", g.getIdLong())
                    )
                    .as(this::parseOne, c);
        }
    }

    @Override
    public List<NamedQuote> getAllQuotesByAuthorInGuild(long u, Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("""
                            SELECT *, (SELECT ARRAY_AGG(flag) FROM quote_flags qf WHERE qf.quote_id = id) AS flags
                            FROM quotes
                            WHERE guild_id = :g
                              AND (SELECT COUNT(*) FROM quote_flags qf WHERE qf.quote_id = id AND flag = :f) = 0
                              AND guild_id=:g AND author_id=:u
                            ORDER BY RANDOM()
                            LIMIT 1""")
                    .on(
                            Param.values("f", Flag.HIDDEN.ordinal()),
                            Param.value("g", g.getIdLong()),
                            Param.value("u", u)
                    )
                    .as(this::parseManyQuotes, c);
        }
    }

    @Override
    public List<StatPoint<Long>> getUniqueAuthorQuoteCountPerGuild(Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("""
                            SELECT author_id, COUNT(author_id) AS "count"
                            FROM quotes
                            WHERE (SELECT COUNT(*) FROM quote_flags qf WHERE qf.quote_id = id AND flag = :f) = 0
                              AND guild_id = :g
                            GROUP BY author_id
                            ORDER BY COUNT(author_id) DESC""")
                    .on(
                            Param.values("f", Flag.HIDDEN.ordinal()),
                            Param.value("g", g.getIdLong())
                    )
                    .as((rs, conn) -> {
                        List<StatPoint<Long>> stats = new ArrayList<>();
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
    public boolean deleteQuote(int id, Guild g, long userId) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            NamedQuote nq = Query.of("SELECT *, (SELECT NULL) AS flags FROM quotes WHERE id=:i AND guild_id=:g")
                    .on(
                            Param.value("i", id),
                            Param.value("g", g.getIdLong())
                    ).as(this::parseOne, c);
            if (nq == null) {
                return false;
            }
            LOGGER.info("Hiding quote #" + nq.getId() + " from guild \"" + g.getName() + "\" with content: " + nq);
            return Query.of("INSERT INTO quote_flags (quote_id, flag, created_by) VALUES (:i, :v, :u)")
                    .on(
                            Param.value("i", id),
                            Param.value("v", Flag.HIDDEN.ordinal()),
                            Param.value("u", userId)
                    ).executeInsert(c).orElse(0L) > 0;
        }
    }

    // quote randomization handled by SQL's ORDER BY RANDOM()
    private List<NamedQuote> parseManyQuotes(ResultSet rs, Connection c) {
        List<NamedQuote> quotes = parseMany(rs, c, this::parseOne);
        if (quotes.isEmpty() || quotes.size() == 1) {
            // sorting not required
            return quotes;
        }
        // TODO can sort can just be called???
        List<NamedQuote> frontQuotes = new ArrayList<>();
        List<NamedQuote> backQuotes = new ArrayList<>();
        for (NamedQuote quote : quotes) {
            if (quote.getFlags().contains(Flag.DERANK)) {
                backQuotes.add(quote);
            }
            else {
                frontQuotes.add(quote);
            }
        }
        frontQuotes.addAll(backQuotes);
        return Collections.unmodifiableList(frontQuotes);
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
        if (rs.getArray("flags") != null) {
            nq.setFlags(
                    Arrays.stream(((Integer[]) rs.getArray("flags").getArray()))
                            .map(i -> Flag.values()[i])
                            .collect(Collectors.toCollection(() -> EnumSet.noneOf(Flag.class)))
            );
        }
        return nq;
    }
}
