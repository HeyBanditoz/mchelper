package io.banditoz.mchelper.database;

import java.math.BigDecimal;

import org.jetbrains.annotations.NotNull;

public record LotteryEntrant(long userId, BigDecimal amount) implements Comparable<LotteryEntrant> {
    @Override
    public int compareTo(@NotNull LotteryEntrant o) {
        return amount.compareTo(o.amount);
    }
}
