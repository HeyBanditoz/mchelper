package io.banditoz.mchelper.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ListUtils {
    /**
     * Pulls howMany random entries from the List (l) and builds a String from it.
     *
     * @param howMany The number of random entries to pull
     * @param l   The List containing Strings to extract from
     * @return A built String containing the entry(s) pulled from the list.
     * @throws IllegalArgumentException If the number of entries to grab from the list is bigger than the list itself.
     */
    public static String extractNumRandomly(int howMany, List<String> l, Random random) {
        List<String> localList = new ArrayList<>(l);
        if (howMany <= 0) {
            throw new IllegalArgumentException("Cannot pick zero elements from a list.");
        }
        if (howMany > localList.size()) {
            throw new IllegalArgumentException("Cannot pick " + howMany + " elements from a list with a size of " + localList.size() + ".");
        }
        if (localList.size() == 1) {
            return l.get(0);
        }
        StringBuilder results = new StringBuilder();
        for (int i = 1; ; i++) {
            int pos = random.nextInt(localList.size());
            results.append(localList.get(pos));
            if (i == howMany) {
                return results.toString();
            }
            results.append(", ");
            localList.remove(pos);
        }
    }
}
