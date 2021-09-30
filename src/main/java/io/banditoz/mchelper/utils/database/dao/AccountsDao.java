package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.money.MoneyException;
import io.banditoz.mchelper.utils.database.StatPoint;
import io.banditoz.mchelper.utils.database.Transaction;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface AccountsDao {
    BigDecimal queryBalance(long id) throws SQLException;
    boolean accountExists(long id) throws SQLException;
    void transferTo(BigDecimal amount, long from, long to, Transaction t) throws SQLException;
    void change(BigDecimal amount, long id, Transaction t, boolean add) throws SQLException;
    List<StatPoint<Long, BigDecimal>> getLeaderboard() throws SQLException;
    List<Transaction> getNTransactionsForUser(long id, int n) throws SQLException, MoneyException;
    List<Long> getAllAccounts() throws SQLException;
}
