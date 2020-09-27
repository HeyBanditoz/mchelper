package io.banditoz.mchelper.utils.database;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserStat userStat = (UserStat) o;
        return userId == userStat.userId &&
                count == userStat.count;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, count);
    }
}
