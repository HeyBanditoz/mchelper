package io.banditoz.mchelper.utils.database;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single transaction for user accounts.
 *
 * @param from Where the money came from. Null if from the bot
 * @param to Where the money went. Null if to the bot.
 * @param before How much money the account has before the transaction
 * @param amount How much money was added or removed for this transaction.
 * @param memo The memo associated with this transaction.
 * @param date The {@link java.time.LocalDateTime} when this transaction was created.
 * @param type The {@link Type} of this transfer
 */
public record Transaction(
        @Nullable Long from,
        @Nullable Long to,
        BigDecimal before,
        BigDecimal amount,
        String memo,
        LocalDateTime date,
        Type type
) implements Comparable<Transaction> {
    public static Transaction of(Long from, Long to, BigDecimal before, BigDecimal amount, LocalDateTime date, String memo) {
        date = Objects.requireNonNullElseGet(date, LocalDateTime::now);
        Type t;
        if (from == null && to == null) {
            throw new IllegalArgumentException("from and to cannot be null!");
        }
        else if (from == null) {
            t = Type.GRANT;
        }
        else if (to == null) {
            t = Type.REVOKE;
        }
        else {
            t = Type.TRANSFER;
        }
        return new Transaction(from, to, before, amount, memo, date, t);
    }

    public BigDecimal getFinalAmount() {
        return before.add(amount);
    }

    @Override
    public int compareTo(@Nonnull Transaction o) {
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
