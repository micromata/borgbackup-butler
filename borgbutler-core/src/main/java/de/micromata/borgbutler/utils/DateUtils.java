package de.micromata.borgbutler.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    /**
     * @param borgDateTime
     * @return
     */
    public static String get(String borgDateTime) {
        LocalDateTime dateTime = LocalDateTime.parse(borgDateTime);
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
