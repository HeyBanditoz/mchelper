package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.money.MoneyException;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.StatPoint;
import io.banditoz.mchelper.utils.database.Transaction;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The Dao which holds methods to manipulate monetary accounts.
 *
 * Do <i>not</i> instantiate destructive methods this class. It is not synchronized. Use the synchronous
 * {@link io.banditoz.mchelper.MCHelper#getAccountManager()} instead.
 */
public class AccountsDaoImpl extends Dao implements AccountsDao {
    private static final BigDecimal SEED_MONEY = new BigDecimal("1000");

    public AccountsDaoImpl(Database database) {
        super(database);
    }

    @Override
    public String getSqlTableGenerator() {
        return "CREATE TABLE IF NOT EXISTS `accounts`( `id` bigint(18) NOT NULL, `balance` decimal(13,2) DEFAULT NULL, `created_at` datetime NOT NULL DEFAULT current_timestamp(), PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; CREATE TABLE IF NOT EXISTS `transactions`( `from_id` bigint(18) DEFAULT NULL, `to_id` bigint(18) DEFAULT NULL, `before` decimal(13,2) NOT NULL, `amount` decimal(13,2) NOT NULL, `memo` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL, `when` datetime NOT NULL DEFAULT current_timestamp()) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    }

    @Override
    public BigDecimal queryBalance(long id) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            if (!accountExists(id)) {
                createAccount(c, id);
            }
            PreparedStatement ps = c.prepareStatement("SELECT * FROM accounts WHERE id=?");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            BigDecimal balance = rs.getBigDecimal(2);
            rs.close();
            ps.close();
            return balance;
        }
    }

    @Override
    public boolean accountExists(long id) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM accounts WHERE id=?");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            boolean exist = rs.isBeforeFirst();
            rs.close();
            ps.close();
            return exist;
        }
    }

    @Override
    public void transferTo(BigDecimal amount, long from, long to, Transaction t) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            // TODO ensure Transaction matches the amount?
            c.setAutoCommit(false);
            PreparedStatement ps = c.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE id = ?");
            ps.setBigDecimal(1, amount);
            ps.setLong(2, from);
            ps.execute();
            ps.close();

            ps = c.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE id = ?");
            ps.setBigDecimal(1, amount);
            ps.setLong(2, to);
            ps.execute();
            ps.close();

            if (t != null) {
                log(t, c);
            }
            c.commit();
        }
    }

    @Override
    public void change(BigDecimal amount, long id, Transaction t, boolean add) throws SQLException {
        // TODO ensure Transaction matches the amount?
        try (Connection c = DATABASE.getConnection()) {
            c.setAutoCommit(false);
            PreparedStatement ps = c.prepareStatement("UPDATE accounts SET balance = balance " + (add ? '+' : '-') + " ? WHERE id = ?");
            ps.setBigDecimal(1, amount);
            ps.setLong(2, id);
            ps.execute();
            ps.close();
            if (t != null) {
                log(t, c);
            }
            c.commit();
        }
    }

    @Override
    public List<StatPoint<Long, BigDecimal>> getLeaderboard() throws SQLException {
        List<StatPoint<Long, BigDecimal>> leaderboard = new ArrayList<>();
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT id, balance FROM accounts ORDER BY balance DESC;");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                leaderboard.add(new StatPoint<>(rs.getLong(1), rs.getBigDecimal(2)));
            }
            ps.close();
            rs.close();
        }
        return leaderboard;
    }

    private void log(Transaction t, Connection c) throws SQLException {
        PreparedStatement ps = c.prepareStatement("INSERT INTO transactions VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP())");
        if (t.getFrom() == null) {
            ps.setNull(1, Types.BIGINT);
        }
        else {
            ps.setLong(1, t.getFrom());
        }
        if (t.getTo() == null) {
            ps.setNull(2, Types.BIGINT);
        }
        else {
            ps.setLong(2, t.getTo());
        }
        ps.setBigDecimal(3, t.getBefore());
        ps.setBigDecimal(4, t.getAmount());
        ps.setString(5, t.getMemo());
        ps.execute();
        ps.close();
    }

    @Override
    public List<Transaction> getNTransactionsForUser(long id, int n) throws SQLException, MoneyException {
        if (n <= 0) {
            throw new IllegalArgumentException("Need to fetch at least one transaction!");
        }
        List<Transaction> txns = new ArrayList<>(Math.min(n, 10000)); // hacky
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM transactions WHERE from_id=? OR to_id=? ORDER BY `when` DESC LIMIT ?");
            ps.setLong(1, id);
            ps.setLong(2, id);
            ps.setLong(3, n);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                txns.add(createTransactionFromResultSet(rs));
            }
            rs.close();
            ps.close();
        }
        if (txns.isEmpty()) {
            throw new MoneyException("There is no transaction history for " + id);
        }
        txns.sort(Transaction::compareTo);
        return txns;
    }

    @Override
    public List<Long> getAllAccounts() throws SQLException {
        List<Long> accs = new ArrayList<>();
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT id FROM accounts");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                accs.add(rs.getLong(1));
            }
            rs.close();
            ps.close();
        }
        return accs;
    }

    /**
     * Creates a new account with the current SEED_MONEY value.
     *
     * @param c  The {@link Connection} to use.
     * @param id The ID to associate the account with.
     * @throws SQLException If there was a problem creating the account.
     */
    private void createAccount(Connection c, long id) throws SQLException {
        PreparedStatement ps = c.prepareStatement("INSERT INTO accounts VALUES (?, ?, CURRENT_TIMESTAMP())");
        ps.setLong(1, id);
        ps.setBigDecimal(2, BigDecimal.ZERO);
        ps.execute();
        ps.close();
        change(SEED_MONEY, id, new Transaction(null, id, BigDecimal.ZERO, SEED_MONEY, null, "seed money"), true);
    }

    public static Transaction createTransactionFromResultSet(ResultSet rs) throws SQLException {
        Long from = rs.getLong(1);
        Long to = rs.getLong(2);
        BigDecimal before = rs.getBigDecimal(3);
        BigDecimal amount = rs.getBigDecimal(4);
        String memo = rs.getString(5);
        LocalDateTime time = rs.getTimestamp(6).toLocalDateTime();
        return new Transaction(from == 0 ? null : from, to == 0 ? null : to, before, amount, time, memo);
    }
}
