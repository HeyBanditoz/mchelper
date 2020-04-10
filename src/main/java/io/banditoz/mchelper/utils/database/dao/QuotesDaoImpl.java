package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.NamedQuote;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuotesDaoImpl extends Dao implements QuotesDao {
    @Override
    public String getSqlTableGenerator() {
        return "CREATE TABLE IF NOT EXISTS `quotes`( `guild_id` bigint(18) NOT NULL, `author_id` bigint(18) NOT NULL, `quote` varchar(1500) COLLATE utf8mb4_unicode_ci NOT NULL, `quote_author` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL, `last_modified` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;";
    }

    @Override
    public void saveQuote(NamedQuote nq) throws SQLException {
        try (Connection c = Database.getConnection()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO quotes VALUES (?, ?, ?, ?, (SELECT NOW()))");
            ps.setLong(1, nq.getGuildId());
            ps.setLong(2, nq.getAuthorId());
            ps.setString(3, nq.getQuote());
            ps.setString(4, nq.getQuoteAuthor());
            ps.execute();
            ps.close();
        }
    }

    @Override
    public List<NamedQuote> getQuotesByMatch(String search, Guild g) throws SQLException {
        ArrayList<NamedQuote> quotes = new ArrayList<>();
        search = "%" + search + "%";
        try (Connection c = Database.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM quotes WHERE guild_id=? AND quote LIKE ? OR quote_author LIKE ?");
            ps.setLong(1, g.getIdLong());
            ps.setString(2, search);
            ps.setString(3, search);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                quotes.add(buildQuoteFromResultSet(rs));
            }
            rs.close();
            ps.close();
        }
        return quotes;
    }

    @Override
    public List<NamedQuote> getQuotes(Guild g) throws SQLException {
        ArrayList<NamedQuote> quotes = new ArrayList<>();
        try (Connection c = Database.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM quotes WHERE guild_id=?");
            ps.setLong(1, g.getIdLong());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                quotes.add(buildQuoteFromResultSet(rs));
            }
            rs.close();
            ps.close();
        }
        return quotes;
    }

    private NamedQuote buildQuoteFromResultSet(ResultSet rs) throws SQLException {
        NamedQuote nq = new NamedQuote();
        nq.setGuildId(rs.getLong("guild_id"));
        nq.setAuthorId(rs.getLong("author_id"));
        nq.setQuote(rs.getString("quote"));
        nq.setQuoteAuthor(rs.getString("quote_author"));
        nq.setLastModified(rs.getTimestamp("last_modified"));
        return nq;
    }
}
