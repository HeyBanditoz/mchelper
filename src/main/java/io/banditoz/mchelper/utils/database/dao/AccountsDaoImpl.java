package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Transaction;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.StatPoint;

import java.math.BigDecimal;
import java.sql.*;
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
    public void subtract(BigDecimal amount, long id) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE id=?");
            ps.setBigDecimal(1, amount);
            ps.setLong(2, id);
            ps.execute();
            ps.close();
        }
    }

    @Override
    public void transferTo(BigDecimal amount, long from, long to) throws SQLException {
        subtract(amount, from);
        add(amount, to);
    }

    @Override
    public void add(BigDecimal amount, long id) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE id=?");
            ps.setBigDecimal(1, amount);
            ps.setLong(2, id);
            ps.execute();
            ps.close();
        }
    }

    @Override
    public List<StatPoint<Long, BigDecimal>> getLeaderboard() throws SQLException {
        List<StatPoint<Long, BigDecimal>> leaderboard = new ArrayList<>();
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT id, balance FROM accounts;");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                leaderboard.add(new StatPoint<>(rs.getLong(1), rs.getBigDecimal(2)));
            }
            ps.close();
            rs.close();
        }
        leaderboard.sort(StatPoint::compareTo);
        return leaderboard;
    }

    @Override
    public void log(Transaction t) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
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
    }

    @Override
    public List<Transaction> getNTransactionsForUser(long id, int n) throws SQLException {
        List<Transaction> txns = new ArrayList<>(Math.min(n, 10000)); // hacky
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM transactions WHERE from_id=? OR to_id=? LIMIT ?");
            ps.setLong(1, id);
            ps.setLong(2, id);
            ps.setLong(3, n);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                txns.add(Transaction.of(rs));
            }
            rs.close();
            ps.close();
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
        log(new Transaction(null, id, BigDecimal.ZERO, SEED_MONEY, null, "seed money"));
        add(SEED_MONEY, id);
    }
}
