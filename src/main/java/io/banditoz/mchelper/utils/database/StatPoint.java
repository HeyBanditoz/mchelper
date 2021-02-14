package io.banditoz.mchelper.utils.database;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static io.banditoz.mchelper.utils.StringUtils.padZeros;
import static io.banditoz.mchelper.utils.StringUtils.truncate;

/**
 * A simple class which wraps a type around a count. Implements a {@link java.util.Comparator} where the highest thing
 * will be first when comparing.
 *
 * @param <T> The type of this statistic point.
 */
public class StatPoint<T extends Comparable<T>, V extends Comparable<V>> implements Comparable<StatPoint<T, V>> {
    private final T thing;
    private final V count;

    public StatPoint(T thing, V count) {
        this.thing = thing;
        this.count = count;
    }

    public T getThing() {
        return thing;
    }

    public V getCount() {
        return count;
    }

    @Override
    public int compareTo(@NotNull StatPoint<T, V> o) {
        if (count == o.count) {
            return thing.compareTo(o.thing);
        }
        else {
            return (o.count.compareTo(count)); // reverse, so the one with the biggest count is the first
        }
    }

    /**
     * Generates a pretty formatted table containing a leaderboard for an already sorted StatPoint list, with
     * customization. It is recommended you pad zeroes in your thingFormatter. For example, a table could look like this
     * (see {@link io.banditoz.mchelper.commands.StatisticsCommand}'s generateStatsTable for how this was exactly done.
     * Note that these tables look best if in an {@link net.dv8tion.jda.api.entities.MessageEmbed}'s description field.
     * <pre>
     * ```Rank  Name
     * 1.    DoubleOrNothing      391
     * 2.    Balance              205
     * 3.    Transfer             141
     * 4.    Bet                  106
     * 5.    Eval                 82
     * ```
     * </pre>
     *
     * @param list           The list to build a leaderboard from.
     * @param thingLength    The maximum length of the thing before it gets truncated down.
     * @param thingFormatter How to format the Thing in the leaderboard.
     * @param countFormatter How to format the Count in the leaderboard.
     * @param <T>            The Thing type.
     * @param <V>            The Count type.
     * @return A pretty formatted table (ideally for use in an embed.)
     */
    public static <T extends Comparable<T>, V extends Comparable<V>> String statsToPrettyLeaderboard(List<StatPoint<T, V>> list, int thingLength, Function<T, String> thingFormatter, Function<V, String> countFormatter) {
        Objects.requireNonNull(list, "The list cannot be null!");
        Objects.requireNonNull(thingFormatter, "The thingFormatter cannot be null!");
        Objects.requireNonNull(countFormatter, "The countFormatter cannot be null!");

        StringBuilder sb = new StringBuilder("\n```Rank  Name\n");
        for (int i = 1; i < list.size(); i++) {
            StatPoint<T, V> point = list.get(i - 1);
            String name = padZeros(truncate(thingFormatter.apply(point.thing), thingLength, false), thingLength + 4);
            sb.append(padZeros(String.valueOf(i) + '.', 5)).append(name);
            sb.append(countFormatter.apply(point.count)).append('\n');
        }
        return sb.toString() + "```";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StatPoint<?, ?> point = (StatPoint<?, ?>) o;
        return count == point.count &&
                thing.equals(point.thing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(thing, count);
    }

    @Override
    public String toString() {
        return thing.toString() + ": " + count.toString();
    }
}
