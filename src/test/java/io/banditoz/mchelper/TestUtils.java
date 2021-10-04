package io.banditoz.mchelper;

public class TestUtils {
    public static boolean containsString(String[] expected, String actual) {
        for (String s : expected) {
            if (s.equals(actual)) {
                return true;
            }
        }
        return false;
    }
}
