package io.banditoz.mchelper.utils;

import java.security.SecureRandom;
import java.util.ArrayList;

public class ListUtils {
    private static SecureRandom random = new SecureRandom();

    public static String extractNumRandomly(int num, ArrayList<String> l) {
        StringBuilder results = new StringBuilder();
        for (int i = 0; i < num; i++) {
            int pos = random.nextInt(l.size());
            results.append(l.get(pos));
            if (i < num - 1) {
                results.append(", ");
            }
            l.remove(pos);
        }
        String s = results.toString();
        return s;
    }
}
