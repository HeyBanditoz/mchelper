package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.money.MoneyException;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.StatPoint;
import io.banditoz.mchelper.utils.database.Transaction;
import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Query;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Dao which holds methods to manipulate monetary accounts.
 *
 * Do <i>not</i> instantiate this class. It is not synchronized. Use the synchronous
 * {@link io.banditoz.mchelper.MCHelper#getAccountManager()} instead.
 */
public class AccountsDaoImpl extends Dao implements AccountsDao {
    private static final BigDecimal SEED_MONEY = new BigDecimal("1000");

    public AccountsDaoImpl(Database database) {
        super(database);
    }

    @Override
    public String getSqlTableGenerator() {
        return """
                CREATE TABLE IF NOT EXISTS accounts (
                    id bigint NOT NULL,
                    balance numeric(13,2) DEFAULT NULL::numeric,
                    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    PRIMARY KEY (id)
                );
                
                CREATE TABLE IF NOT EXISTS transactions (
                    from_id bigint,
                    to_id bigint,
                    before numeric(13,2) NOT NULL,
                    amount numeric(13,2) NOT NULL,
                    memo character varying(100) NOT NULL,
                    "when" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
                );
                """;
    }

    @Override
    public BigDecimal queryBalance(long id, boolean allowCreation) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT * FROM accounts WHERE id=:i;")
                    .on(Param.value("i", id))
                    .as((rs, conn) -> {
                        if (rs.next()) {
                            return rs.getBigDecimal(2);
                        }
                        else if (allowCreation) {
                            return createAccount(c, id);
                        }
                        else {
                            return null;
                        }
                    }, c);
        }
    }

    @Override
    public boolean accountExists(long id) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT id FROM accounts WHERE id=:i")
                    .on(Param.value("i", id))
                    .as((rs, conn) -> rs.isBeforeFirst(), c);
        }
    }

    @Override
    public void transferTo(BigDecimal amount, long from, long to, Transaction t) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            c.setAutoCommit(false);
            Query.of("UPDATE accounts SET balance = balance - :b WHERE id = :i;")
                    .on(
                            Param.value("b", amount),
                            Param.value("i", from)
                    ).executeUpdate(c);
            Query.of("UPDATE accounts SET balance = balance + :b WHERE id = :i;")
                    .on(
                            Param.value("b", amount),
                            Param.value("i", to)
                    ).executeUpdate(c);
            if (t != null) {
                log(t, c);
            }
            c.commit();
        }
    }

    @Override
    public void change(BigDecimal amount, long id, Transaction t, boolean add) throws SQLException {
        if (t.amount().abs().compareTo(amount.abs()) != 0) {
            throw new IllegalArgumentException("The transaction amount (" + t.amount() + ") does not match the amount (" + amount + ")!");
        }
        try (Connection c = DATABASE.getConnection()) {
            c.setAutoCommit(false);
            Query.of("UPDATE accounts SET balance = balance " + (add ? '+' : '-') + " :a WHERE id = :i")
                    .on(
                            Param.value("a", amount),
                            Param.value("i", id)
                    ).execute(c);
            log(t, c);
            c.commit();
        }
    }

    @Override
    public List<StatPoint<Long>> getLeaderboard() throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT id, balance FROM accounts ORDER BY balance DESC;")
                    .as((rs, conn) -> {
                        List<StatPoint<Long>> leaderboard = new ArrayList<>();
                        while (rs.next()) {
                            leaderboard.add(new StatPoint<>(rs.getLong(1), rs.getBigDecimal(2)));
                        }
                        return leaderboard;
                    }, c);
        }
    }

    private void log(Transaction t, Connection c) throws SQLException {
        Query.of("INSERT INTO transactions VALUES (:f, :t, :b, :a, :m)")
                .on(
                        Param.value("f", Optional.ofNullable(t.from())),
                        Param.value("t", Optional.ofNullable(t.to())),
                        Param.value("b", t.before()),
                        Param.value("a", t.amount()),
                        Param.value("m", t.memo())
                ).execute(c);
    }

    @Override
    public List<Transaction> getNTransactionsForUser(long id, int n) throws SQLException, MoneyException {
        if (n <= 0) {
            throw new IllegalArgumentException("Need to fetch at least one transaction!");
        }
        try (Connection c = DATABASE.getConnection()) {
            List<Transaction> txns = Query.of("SELECT * FROM transactions WHERE from_id=:f OR to_id=:f ORDER BY \"when\" DESC LIMIT :l;")
                    .on(
                            Param.value("f", id),
                            Param.value("l", n)
                    ).as((rs, conn) -> {
                        List<Transaction> l = new ArrayList<>(Math.min(n, 10000)); // hacky
                        while (rs.next()) {
                            l.add(createTransactionFromResultSet(rs));
                        }
                        return l;
                    }, c);
            if (txns.isEmpty()) {
                throw new MoneyException("There is no transaction history for " + id);
            }
            txns.sort(Transaction::compareTo);
            return txns;
        }
    }

    @Override
    public List<Long> getAllAccounts() throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT id FROM accounts;")
                    .as((rs, conn) -> {
                        List<Long> accs = new ArrayList<>();
                        while (rs.next()) {
                            accs.add(rs.getLong(1));
                        }
                        return accs;
                    }, c);
        }
    }

    /**
     * Creates a new account with the current SEED_MONEY value.
     *
     * @param c  The {@link Connection} to use.
     * @param id The ID to associate the account with.
     * @throws SQLException If there was a problem creating the account.
     */
    private BigDecimal createAccount(Connection c, long id) throws SQLException {
        Query.of("INSERT INTO accounts VALUES (:i, :b);")
                .on(
                        Param.value("i", id),
                        Param.value("b", BigDecimal.ZERO)
                ).execute(c);
        change(SEED_MONEY, id, Transaction.of(null, id, BigDecimal.ZERO, SEED_MONEY, null, "seed money"), true);
        return SEED_MONEY;
    }

    public static Transaction createTransactionFromResultSet(ResultSet rs) throws SQLException {
        long from = rs.getLong(1);
        long to = rs.getLong(2);
        BigDecimal before = rs.getBigDecimal(3);
        BigDecimal amount = rs.getBigDecimal(4);
        String memo = rs.getString(5);
        LocalDateTime time = rs.getTimestamp(6).toLocalDateTime();
        return Transaction.of(from == 0 ? null : from, to == 0 ? null : to, before, amount, time, memo);
    }
}
