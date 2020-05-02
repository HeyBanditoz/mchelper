package io.banditoz.mchelper.utils;

import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.TimeUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SnowflakeUtils {
    private static final Pattern PATTERN = Pattern.compile("\\d{18}");

    public static String returnDateTimesForIDs(String possibleMatches) {
        StringBuilder sb = new StringBuilder("```");
        Matcher m = PATTERN.matcher(possibleMatches);
        while (m.find()) {
            sb.append(m.group())
                    .append(" -> ")
                    .append(TimeUtil.getTimeCreated(MiscUtil.parseSnowflake(m.group())).toString())
                    .append('\n');
        }
        return sb.append("```").toString().equals("``````") ? "No snowflake IDs found or IDs are invalid." : sb.toString();
    }
}
