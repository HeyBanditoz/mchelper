package io.banditoz.mchelper.database;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

import io.banditoz.mchelper.money.AccountManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

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

    public Long fromN() {
        return from == null ? 0 : from;
    }

    public Long toN() {
        return to == null ? 0 : to;
    }

    @Override
    public int compareTo(@Nonnull Transaction o) {
        return this.date.compareTo(o.date);
    }

    public MessageEmbed render(JDA jda) {
        // wtf?
        User fromUser = null;
        User toUser = null;
        try {
            fromUser = jda.getUserById(from());
        } catch (NullPointerException ignored) {}
        try {
            toUser = jda.getUserById(to());
        } catch (NullPointerException ignored) {}
        String tType = switch (type) {
            case TRANSFER -> "Transfer";
            case GRANT -> "Grant";
            case REVOKE -> "Revoke";
        };
        return new EmbedBuilder()
                .setTitle("Transaction")
                .setDescription(tType)
                .addField("From", (fromUser == null) ? jda.getSelfUser().getAsMention() : fromUser.getAsMention(), false)
                .addField("To", (toUser == null) ? jda.getSelfUser().getAsMention() : toUser.getAsMention(), false)
                .addField("Amount", '$' + AccountManager.format(amount.abs()), false)
                .addField("Memo", memo(), false)
                .setColor(getColorFromTransaction())
                .setTimestamp(date().atZone(ZoneId.systemDefault()))
                .build();
    }

    private Color getColorFromTransaction() {
        switch (type) {
            case TRANSFER -> {
                return Color.CYAN;
            }
            case GRANT -> {
                return Color.GREEN;
            }
            case REVOKE -> {
                return Color.RED;
            }
        }
        return null;
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
