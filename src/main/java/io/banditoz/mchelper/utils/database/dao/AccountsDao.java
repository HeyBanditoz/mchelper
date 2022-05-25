package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.MoneyException;
import io.banditoz.mchelper.utils.database.StatPoint;
import io.banditoz.mchelper.utils.database.Transaction;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface AccountsDao {
    /**
     * Returns a user's balance, or null if no account exists.
     *
     * @param id            The user ID to check.
     * @param allowCreation If an account should be created.
     * @return A {@link BigDecimal} representing their amount, null if no account exists.
     * @throws SQLException If there was a database error.
     */
    BigDecimal queryBalance(long id, boolean allowCreation) throws SQLException;

    /**
     * Checks if an account exists.
     *
     * @param id The user ID to check.
     * @return true if the account exists, false otherwise.
     * @throws SQLException If there was a database error.
     */
    boolean accountExists(long id) throws SQLException;

    /**
     * Transfers money to another user, creating a transaction while doing so.
     * You should check if the sender has money to send first before calling this!
     * (The {@link AccountManager#transferTo(BigDecimal, long, long, String) transfer} method does this already.)
     *
     * @param amount The amount to send.
     * @param from   The sender's user ID.
     * @param to     The recipient's user ID.
     * @param t      The {@link Transaction} to use as the base for the transfer/
     * @throws SQLException If there was a database error.
     */
    void transferTo(BigDecimal amount, long from, long to, Transaction t) throws SQLException;

    /**
     * Changes an account's balance, creating a transaction while doing so. <i>Note!</i> If you're removing money, you
     * should call {@link io.banditoz.mchelper.money.AccountManager#checkAmount(BigDecimal, BigDecimal) checkAmount}
     * from the singleton {@link AccountManager} instance before removing money, unless you want the account to go in
     * the negative.
     *
     * @param amount The amount to either add or remove.
     * @param id     The affected user ID.
     * @param t      The {@link Transaction} to use as the base for the account change. Ensure the amount in the
     *               transaction matches what's in the amount.
     * @param add    Whether we are adding or subtracting money.
     * @throws SQLException If there was a database error.
     */
    void change(BigDecimal amount, long id, Transaction t, boolean add) throws SQLException;

    /**
     * @return the money leaderboard (as a {@link StatPoint}) for all accounts.
     * @throws SQLException If there was a database error.
     */
    List<StatPoint<Long>> getLeaderboard() throws SQLException;

    /**
     * @param id The user to fetch transactions for.
     * @param n  The maximum number of transactions to retrieve, starting from their most latest.
     * @return A {@link List} of {@link Transaction transactions}.
     * @throws SQLException   If there was a database error.
     * @throws MoneyException If the user has no transaction history (most likely no account.)
     */
    List<Transaction> getNTransactionsForUser(long id, int n) throws SQLException, MoneyException;

    /**
     * @return A {@link List} of user accounts <i>without</i> balance associated.
     * @throws SQLException If there was a database error.
     */
    List<Long> getAllAccounts() throws SQLException;
}
