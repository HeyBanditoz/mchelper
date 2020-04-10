package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.NamedQuote;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.SQLException;
import java.util.List;

public interface QuotesDao {
    void saveQuote(NamedQuote nq) throws SQLException;
    List<NamedQuote> getQuotesByMatch(String search, Guild g) throws SQLException;
    List<NamedQuote> getQuotes(Guild g) throws SQLException;
}
