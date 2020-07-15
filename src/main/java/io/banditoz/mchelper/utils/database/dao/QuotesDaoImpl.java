package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.NamedQuote;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class QuotesDaoImpl extends Dao implements QuotesDao {
    public QuotesDaoImpl(Database database) {
        super(database);
    }

    @Override
    public String getSqlTableGenerator() {
        return "CREATE TABLE IF NOT EXISTS `quotes`( `guild_id` bigint(18) NOT NULL, `author_id` bigint(18) NOT NULL, `quote` varchar(1500) COLLATE utf8mb4_unicode_ci NOT NULL, `quote_author` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL, `last_modified` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;";
    }

    @Override
    public void saveQuote(NamedQuote nq) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO `quotes` VALUES (?, ?, ?, ?, (SELECT NOW()))");
            ps.setLong(1, nq.getGuildId());
            ps.setLong(2, nq.getAuthorId());
            ps.setString(3, nq.getQuote());
            ps.setString(4, nq.getQuoteAuthor());
            ps.execute();
            ps.close();
        }
    }

    @Override
    public Optional<NamedQuote> getRandomQuoteByMatch(String search, @NotNull Guild g) throws SQLException {
        search = "%" + search + "%";
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM `quotes` WHERE guild_id=? AND (quote LIKE ? OR quote_author LIKE ?) ORDER BY RAND() LIMIT 1");
            ps.setLong(1, g.getIdLong());
            ps.setString(2, search);
            ps.setString(3, search);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                ps.close();
                return buildQuoteFromResultSet(rs);
            }
        }
    }

    @Override
    public Optional<NamedQuote> getRandomQuote(Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM `quotes` WHERE guild_id=? ORDER BY RAND() LIMIT 1");
            ps.setLong(1, g.getIdLong());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                ps.close();
                return buildQuoteFromResultSet(rs);
            }
        }
    }

    @Override
    public Map<Long, Integer> getUniqueAuthorQuoteCountPerGuild(Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT author_id, COUNT(author_id) AS 'count' FROM `quotes` WHERE guild_id=? GROUP BY author_id ORDER BY COUNT(author_id) DESC");
            ps.setLong(1, g.getIdLong());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.isLast()) {
                    LinkedHashMap<Long, Integer> stats = new LinkedHashMap<>();
                    while (rs.next()) {
                        stats.put(rs.getLong("author_id"), rs.getInt("count"));
                    }
                    return stats;
                }
            }
        }
        return Collections.emptyMap();
    }

    private Optional<NamedQuote> buildQuoteFromResultSet(ResultSet rs) throws SQLException {
        NamedQuote nq = new NamedQuote();
        nq.setGuildId(rs.getLong("guild_id"));
        nq.setAuthorId(rs.getLong("author_id"));
        nq.setQuote(rs.getString("quote"));
        nq.setQuoteAuthor(rs.getString("quote_author"));
        nq.setLastModified(rs.getTimestamp("last_modified"));
        return Optional.of(nq);
    }
}
