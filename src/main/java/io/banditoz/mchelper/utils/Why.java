package io.banditoz.mchelper.utils;

import java.security.SecureRandom;
import java.util.ArrayList;

public class Why {
    // stolen from https://github.com/EvilDeaaaadd/owoify/blob/401dd3fc03f85cc6db072c81ad0c7c512e4faff4/src/lib.rs#L35
    private static ArrayList<String> FACES = new ArrayList<>();
    private static SecureRandom random = new SecureRandom();

    static {
        FACES.add("・`ω´・");
        FACES.add("OwO");
        FACES.add("owo");
        FACES.add("oωo");
        FACES.add("òωó");
        FACES.add("°ω°");
        FACES.add("UwU");
        FACES.add(">w<");
        FACES.add("^w^");
    }

    public static String owoify(String s) {
        if (random.nextBoolean() || s.contains("Here is your quote of the day:")) { // 50% chance :)
            return s.replaceAll("(?:r|l)", "w")
                    .replaceAll("(?:R|L)", "W")
                    .replaceAll("n([aeiou])", "ny$1")
                    .replaceAll("N([aeiou])", "Ny$1")
                    .replaceAll("N([AEIOU])", "NY$1")
                    .replaceAll("th", "d")
                    .replaceAll("ove", "uv")
                    + ' ' + ListUtils.extractNumRandomly(1, FACES);
        }
        else {
            return s;
        }
    }
}
