package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.NamedQuote;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.SQLException;

public interface QuotesDao {
    void saveQuote(NamedQuote nq) throws SQLException;
    NamedQuote getRandomQuoteByMatch(String search, Guild g) throws SQLException;
    NamedQuote getRandomQuote(Guild g) throws SQLException;
}
