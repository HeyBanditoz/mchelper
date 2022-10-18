package io.banditoz.mchelper.utils.database;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public record LotteryEntrant(long userId, BigDecimal amount) implements Comparable<LotteryEntrant> {
    @Override
    public int compareTo(@NotNull LotteryEntrant o) {
        return amount.compareTo(o.amount);
    }
}
