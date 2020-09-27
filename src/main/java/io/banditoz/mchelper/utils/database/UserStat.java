package io.banditoz.mchelper.utils.database;

import org.jetbrains.annotations.NotNull;

public class UserStat implements Comparable<UserStat> {
    private final long userId;
    private final int count;

    public UserStat(long userId, int count) {
        this.userId = userId;
        this.count = count;
    }

    public long getUserId() {
        return userId;
    }

    public int getCount() {
        return count;
    }

    @Override
    public int compareTo(@NotNull UserStat o) {
        return Integer.compare(o.count, this.count); // reverse, so the one with the biggest count is the first
    }
}
