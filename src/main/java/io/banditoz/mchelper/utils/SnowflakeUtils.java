package io.banditoz.mchelper.utils;

import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.TimeUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SnowflakeUtils {
    private static final Pattern PATTERN = Pattern.compile("\\d+");

    public static String returnDateTimesForIDs(String possibleMatches) {
        StringBuilder sb = new StringBuilder();
        Matcher m = PATTERN.matcher(possibleMatches);
        while (m.find()) {
            sb.append(m.group())
                    .append(" -> ")
                    .append(TimeFormat.DATE_TIME_LONG.format(TimeUtil.getTimeCreated(MiscUtil.parseSnowflake(m.group()))))
                    .append('\n');
        }
        return sb.toString().isEmpty() ? "No snowflake IDs found or IDs are invalid." : sb.toString();
    }

    public static boolean isLong(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
