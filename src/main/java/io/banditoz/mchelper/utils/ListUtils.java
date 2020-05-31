package io.banditoz.mchelper.utils;

import java.security.SecureRandom;
import java.util.List;

public class ListUtils {
    private static SecureRandom random = new SecureRandom();

    /**
     * Pulls howMany random entries from the List (l) and builds a String from it.
     *
     * @param howMany The number of random entries to pull
     * @param l   The List containing Strings to extract from
     * @return A built String containing the entry(s) pulled from the list.
     * @throws IllegalArgumentException If the number of entries to grab from the list is bigger than the list itself.
     */
    public static String extractNumRandomly(int howMany, List<String> l) {
        if (howMany <= 0) {
            throw new IllegalArgumentException("Cannot pick zero elements from a list.");
        }
        if (howMany > l.size()) {
            throw new IllegalArgumentException("Cannot pick " + howMany + " elements from a list with a size of " + l.size() + ".");
        }
        if (howMany == 1) {
            return l.get(0);
        }
        StringBuilder results = new StringBuilder();
        for (int i = 1; ; i++) {
            int pos = random.nextInt(l.size());
            results.append(l.get(pos));
            if (i == howMany) {
                return results.toString();
            }
            results.append(", ");
            l.remove(pos);
        }
    }
}
