package io.banditoz.mchelper.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    public static String getLocallyZonedRFC1123(LocalDateTime ldt) {
        return ZonedDateTime.ofInstant(ldt, ZoneOffset.UTC, ZoneId.systemDefault()).format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }
}
