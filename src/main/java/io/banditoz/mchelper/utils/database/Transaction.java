package io.banditoz.mchelper.utils.database;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

public class Transaction implements Comparable<Transaction> {
    /** Where the money came from. Null if from the bot. */
    private final Long from;
    /** Where the money went. Null if to the bot. */
    private final Long to;
    /** How much money the account has before the transaction. */
    private final BigDecimal before;
    /** How much money was added or removed for this transaction. */
    private final BigDecimal amount;
    /** The memo associated with this transaction. */
    private final String memo;
    /** The {@link java.time.LocalDateTime} when this transaction was created. */
    private final LocalDateTime date;
    /** The {@link Type} of this transfer. */
    private final Type type;

    public Transaction(Long from, Long to, BigDecimal before, BigDecimal amount, LocalDateTime date, String memo) {
        this.from = from;
        this.to = to;
        this.before = before;
        this.amount = amount;
        this.date = Objects.requireNonNullElseGet(date, LocalDateTime::now);
        this.memo = memo;

        if (from == null && to == null) {
            throw new IllegalArgumentException("from and to cannot be null!");
        }
        else if (from == null) {
            type = Type.GRANT;
        }
        else if (to == null) {
            type = Type.REVOKE;
        }
        else {
            type = Type.TRANSFER;
        }
    }

    public Long getFrom() {
        return from;
    }

    public Long getTo() {
        return to;
    }

    public BigDecimal getBefore() {
        return before;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getMemo() {
        return memo;
    }

    public Type getType() {
        return type;
    }

    public static Transaction of(ResultSet rs) throws SQLException {
        Long from = rs.getLong(1);
        Long to = rs.getLong(2);
        BigDecimal before = rs.getBigDecimal(3);
        BigDecimal amount = rs.getBigDecimal(4);
        String memo = rs.getString(5);
        LocalDateTime time = rs.getTimestamp(6).toLocalDateTime();
        return new Transaction(from == 0 ? null : from, to == 0 ? null : to, before, amount, time, memo);
    }

    @Override
    public int compareTo(@NotNull Transaction o) {
        return this.date.compareTo(o.date);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                from +
                ' ' + to +
                ' ' + before +
                ' ' + amount +
                " '" + memo + '\'' +
                " " + date +
                ' ' + type +
                '}';
    }
}
