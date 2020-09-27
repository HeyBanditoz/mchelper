package io.banditoz.mchelper.utils.database;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A simple class which wraps a type around a count. Implements a {@link java.util.Comparator} where the highest thing
 * will be first when comparing.
 *
 * @param <T> The type of this statistic point.
 */
public class StatPoint<T> implements Comparable<StatPoint<T>> {
    private final T thing;
    private final int count;

    public StatPoint(T thing, int count) {
        this.thing = thing;
        this.count = count;
    }

    public T getThing() {
        return thing;
    }

    public int getCount() {
        return count;
    }

    @Override
    public int compareTo(@NotNull StatPoint o) {
        return (count > o.count) ? -1 : 1; // reverse, so the one with the biggest count is the first
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StatPoint<?> point = (StatPoint<?>) o;
        return count == point.count &&
                thing.equals(point.thing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(thing, count);
    }

    @Override
    public String toString() {
        return thing.toString() + ": " + count;
    }
}
