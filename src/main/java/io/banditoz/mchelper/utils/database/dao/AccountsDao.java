package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Transaction;
import io.banditoz.mchelper.utils.database.StatPoint;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface AccountsDao {
    BigDecimal queryBalance(long id) throws SQLException;
    boolean accountExists(long id) throws SQLException;
    void subtract(BigDecimal amount, long id) throws SQLException;
    void transferTo(BigDecimal amount, long from, long to) throws SQLException;
    void add(BigDecimal amount, long id) throws SQLException;
    List<StatPoint<Long, BigDecimal>> getLeaderboard() throws SQLException;
    void log(Transaction t) throws SQLException;
    List<Transaction> getNTransactionsForUser(long id, int n) throws SQLException;
}
