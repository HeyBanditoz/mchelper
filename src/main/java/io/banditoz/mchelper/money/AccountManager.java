package io.banditoz.mchelper.money;

import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.StatPoint;
import io.banditoz.mchelper.utils.database.Transaction;
import io.banditoz.mchelper.utils.database.dao.AccountsDao;
import io.banditoz.mchelper.utils.database.dao.AccountsDaoImpl;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

/**
 * The class that handles monetary user accounts and their transactions. Use this class
 * (from {@link io.banditoz.mchelper.MCHelper#getAccountManager()}) instead of creating a new one, as this class does
 * synchronization to ensure money remains the same while a thread operates on a user's account.<br><br>
 * In the comments, user and accounts are used synonymously to represent a user's account, which holds a balance.
 */
public class AccountManager {
    private final AccountsDao dao;
    /** The {@link DecimalFormat} to cleanly format {@link BigDecimal BigDecimals} as {@link String Strings}. */
    private final static DecimalFormat DF;

    static {
        DF = new DecimalFormat("#,###.##");
        DF.setMaximumFractionDigits(2); // 0.1 becomes 0.10 instead of 0.1 when formatted
        DF.setMinimumFractionDigits(0);
    }

    public AccountManager(Database database) {
        dao = new AccountsDaoImpl(database);
    }

    /**
     * Returns a user's balance.
     *
     * @param id            The user ID to check.
     * @param allowCreation If we should create an account and returning the new account's balance.
     * @return The balance.
     * @throws Exception If there was a database error, or if the user doesn't have an account (when allowCreation
     *                   is false).
     */
    public BigDecimal queryBalance(long id, boolean allowCreation) throws Exception {
        BigDecimal bal = dao.queryBalance(id, allowCreation);
        if (bal != null) {
            return bal;
        }
        else {
            throw new MoneyException("<@!" + id + "> does not have an account. Use the bal command to create one.");
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
            throw new MoneyException("Cannot transfer to self.");
        }
        amount = scale(amount);
        if (!dao.accountExists(to)) {
            throw new MoneyException("Cannot transfer to <@!" + to + "> as they do not have an account.");
        }
        BigDecimal priorBalance;
        synchronized (this) {
            priorBalance = queryBalance(from, false);
            checkAmount(priorBalance, amount);
            dao.transferTo(amount, from, to, Transaction.of(from, to, priorBalance, amount.negate(), null, memo));
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
        BigDecimal priorBalance;
        synchronized (this) {
            priorBalance = queryBalance(to, false);
            dao.change(amount, to, Transaction.of(null, to, priorBalance, amount, null, memo), true);
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
        BigDecimal priorBalance;
        synchronized (this) {
            priorBalance = queryBalance(from, false);
            checkAmount(priorBalance, amount);
            dao.change(amount, from, Transaction.of(from, null, priorBalance, amount.negate(), null, memo), false);
        }
        return priorBalance.subtract(amount);
    }

    /**
     * Returns a {@link List} of all balances that exist in the database.
     *
     * @return A List of {@link StatPoint} which contain user IDs and their balance.
     * @throws SQLException If there was a problem fetching all the balances.
     */
    public List<StatPoint<Long>> getAllBalances() throws SQLException {
        return dao.getLeaderboard();
    }

    public List<StatPoint<String>> getTopBalancesForGuild(Guild g) throws SQLException {
        return this.getAllBalances().stream()
                .map(point -> {
                    Member m = g.getMemberById(point.getThing());
                    if (m == null) {
                        return null;
                    }
                    else {
                        return new StatPoint<>(m.getEffectiveName(), point.getCount());
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public void checkUserCanCompleteTransaction(User u, BigDecimal test) throws Exception {
        BigDecimal currentBal = queryBalance(u.getIdLong(), false);
        checkAmount(currentBal, test);
    }

    /**
     * Ensures we are changing more than zero from a number, and the account couldn't go under because of the
     * transaction.
     *
     * @param base       The left-hand.
     * @param subtrahend The right-hand.
     * @throws MoneyException If the conditions aren't met.
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

    /**
     * Checks if a {@link User} is shadowbanned.
     *
     * @param u The {@link User} to check.
     * @return If they are shadowbanned or not.
     */
    public boolean isUserShadowbanned(User u) throws SQLException {
        return dao.isUserShadowbanned(u);
    }

    public List<Long> getAccountsWithTxnsInLastNDays(int days) throws SQLException {
        return dao.getAccountsWithTxnsInLastNDays(days);
    }

    /**
     * Ensure a {@link BigDecimal} does not exceed a scale of two.
     *
     * @param d The {@link BigDecimal} to check.
     * @return A scaled {@link BigDecimal}.
     */
    public BigDecimal scale(BigDecimal d) {
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
