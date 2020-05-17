package io.banditoz.mchelper.utils;

public class StringUtils {
    /**
     * Truncates a String down to a specified amount of characters, either adding a ... or a ... (num characters
     * truncated.) at the end of the String.
     *
     * @param s                         The String to truncate
     * @param length                    The length to truncate the String down to.
     * @param reportTruncatedCharacters Whether or not to report how many characters the String omitted.
     * @return The truncated String.
     */
    public static String truncate(String s, int length, boolean reportTruncatedCharacters) {
        if (s.length() > length) {
            if (reportTruncatedCharacters) {
                return s.substring(0, length) + "... (truncated " + Math.abs(length - s.length()) + " chars)";
            }
            else {
                return s.substring(0, length) + "...";
            }
        }
        else {
            return s;
        }
    }
}
