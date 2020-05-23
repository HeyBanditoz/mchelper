package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.NamedQuote;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.SQLException;
import java.util.Optional;

public interface QuotesDao {
    void saveQuote(NamedQuote nq) throws SQLException;
    Optional<NamedQuote> getRandomQuoteByMatch(String search, Guild g) throws SQLException;
    Optional<NamedQuote> getRandomQuote(Guild g) throws SQLException;
}
