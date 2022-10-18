package io.banditoz.mchelper.utils;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

/**
 * @author <a href="https://stackoverflow.com/a/6409791/19609819">Peter Lawrey</a>
 * @param <E> Inner type.
 */
public class RandomCollection<E> {
    private final NavigableMap<Double, E> map = new TreeMap<>();
    private static final Random random = new Random();
    private double total = 0;

    public RandomCollection(Map<E, Double> map) {
        map.forEach((e, aDouble) -> add(aDouble, e));
    }

    public RandomCollection<E> add(double weight, E result) {
        if (weight <= 0) return this;
        total += weight;
        map.put(total, result);
        return this;
    }

    public E next() {
        double value = random.nextDouble() * total;
        return map.higherEntry(value).getValue();
    }
}