package de.micromata.borgbutler.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * @param borgDateTime
     * @return
     */
    public static String format(String borgDateTime) {
        LocalDateTime dateTime = LocalDateTime.parse(borgDateTime);
        return format(dateTime);
    }

    /**
     * @param dateTime
     * @return
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }
}
