package io.banditoz.mchelper.utils;

import java.security.SecureRandom;
import java.util.ArrayList;

public class ListUtils {
    private static SecureRandom random = new SecureRandom();

    /**
     * Pulls num random entries from the ArrayList (l) and builds a String from it.
     *
     * @param num The number of random entries to pull
     * @param l   The ArrayList containing Strings to extract from
     * @return A built String containing the entry(s) pulled from the list.
     */
    public static String extractNumRandomly(int num, ArrayList<String> l) {
        StringBuilder results = new StringBuilder();
        for (int i = 0; i < num; i++) {
            int pos = random.nextInt(l.size());
            results.append(l.get(pos));
            if (i < num - 1) {
                results.append(", "); // append a comma only if it isn't the last element we're getting
            }
            l.remove(pos);
        }
        return results.toString();
    }
}
