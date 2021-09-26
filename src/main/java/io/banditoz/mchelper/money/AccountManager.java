package io.banditoz.mchelper.money;

import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.StatPoint;
import io.banditoz.mchelper.utils.database.Transaction;
import io.banditoz.mchelper.utils.database.dao.AccountsDao;
import io.banditoz.mchelper.utils.database.dao.AccountsDaoImpl;
import io.banditoz.mchelper.utils.database.dao.TasksDaoImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;

public class AccountManager {
    private final AccountsDao dao;
    private final TasksDaoImpl tasks;
    private final static DecimalFormat DF;

    static {
        DF = new DecimalFormat("#,###.##");
        DF.setMaximumFractionDigits(2); // 0.1 becomes 0.10 instead of 0.1 when formatted
        DF.setMinimumFractionDigits(0);
//        DF.setPositivePrefix("");
//        DF.setGroupingSize(3);
//        DF.setDecimalSeparatorAlwaysShown(false);
    }

    public AccountManager(Database database) {
        dao = new AccountsDaoImpl(database);
        tasks = new TasksDaoImpl(database);
    }

    public BigDecimal queryBalance(long id, boolean allowCreation) throws Exception {
        if (allowCreation) {
            return dao.queryBalance(id);
        }
        else if (dao.accountExists(id)) {
            return dao.queryBalance(id);
        }
        else {
            throw new MoneyException(id + " does not have an account");
        }
    }

    /**
     * Transfer money to another user.
     *
     * @param amount The amount to transfer.
     * @param from   Who is initiating the transfer.
     * @param to     The recipient.
     * @param memo   The memo.
     * @return How much money the user has left.
     * @throws Exception If there was a problem with the transfer.
     */
    public BigDecimal transferTo(BigDecimal amount, long from, long to, String memo) throws Exception {
        if (from == to) {
            throw new MoneyException("cannot transfer to self");
        }
        amount = scale(amount);
        if (!dao.accountExists(to)) {
            throw new MoneyException("cannot transfer to " + to + " as they do not have an account");
        }
        BigDecimal priorBalance;
        synchronized (this) {
            priorBalance = queryBalance(from, true);
            checkAmount(priorBalance, amount);
            dao.transferTo(amount, from, to, new Transaction(from, to, priorBalance, amount.negate(), null, memo));
        }
        return priorBalance.subtract(amount);
    }

    /**
     * Creates money, and adds it to their balance.
     *
     * @param amount The amount to create.
     * @param to     The recipient.
     * @param memo   The memo.
     * @return How much money they have left.
     * @throws Exception If there was a problem with money creation.
     */
    public BigDecimal add(BigDecimal amount, long to, String memo) throws Exception {
        amount = scale(amount);
        if (!dao.accountExists(to)) {
            throw new MoneyException(to + " does not have an account");
        }
        BigDecimal priorBalance;
        synchronized (this) {
            priorBalance = queryBalance(to, false);
            //checkAmount(priorBalance, amount);
            dao.change(amount, to, new Transaction(null, to, priorBalance, amount, null, memo), true);
        }
        return priorBalance.add(amount);
    }

    /**
     * Subtracts money from a user's balance.
     *
     * @param amount The amount to subtract.
     * @param from   The unfortunate debtor.
     * @param memo   The memo.
     * @return How much money they have left.
     * @throws Exception If there was a problem with money creation.
     */
    public BigDecimal remove(BigDecimal amount, long from, String memo) throws Exception {
        amount = scale(amount);
        if (!dao.accountExists(from)) {
            throw new MoneyException(from + " does not have an account");
        }
        BigDecimal priorBalance;
        synchronized (this) {
            priorBalance = queryBalance(from, false);
            checkAmount(priorBalance, amount);
            dao.change(amount, from, new Transaction(from, null, priorBalance, amount.negate(), null, memo), false);
        }
        return priorBalance.add(amount);
    }

    /**
     * Returns a {@link List} of all balances that exist in the database.
     *
     * @return A List of {@link StatPoint} which contain user IDs and their balance.
     * @throws SQLException If there was a problem fetching all the balances.
     */
    public List<StatPoint<Long, BigDecimal>> getAllBalances() throws SQLException {
        return dao.getLeaderboard();
    }

    /**
     * Get a list of all user IDs that have a balance.
     *
     * @return A list of all user IDs.
     * @throws SQLException If there was a problem fetching all the IDs.
     */
    public List<Long> getAllAccounts() throws SQLException {
        return dao.getAllAccounts();
    }

    public LocalDateTime whenCanDoTask(long id, Task t) throws SQLException {
        return tasks.getWhenCanExecute(id, t);
    }

    /**
     * Ensures
     *
     * @param base
     * @param subtrahend
     * @throws MoneyException
     */
    private void checkAmount(BigDecimal base, BigDecimal subtrahend) throws MoneyException {
        if (subtrahend.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MoneyException("must change more than zero!");
        }
        BigDecimal newAmount = base.subtract(subtrahend);
        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new MoneyException("this change would put the account under by $" + format(newAmount.abs()));
        }
    }

    private BigDecimal scale(BigDecimal d) {
        return d.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Formats a BigDecimal to a String, with proper comma grouping. Ensures the scale is 0 or 2 depending on if a
     * decimal is present.
     *
     * @param d The {@link BigDecimal} to format.
     * @return The formatted number as a {@link String}.
     */
    public static String format(BigDecimal d) {
        d = d.stripTrailingZeros();
        // other threads *could* potentially access this and change minimumFractionDigits, so synchronize it
        synchronized (DF) {
            DF.setMinimumFractionDigits(d.scale() > 0 ? 2 : 0);
            return DF.format(d);
        }
    }
}
