package de.micromata.borgbutler.utils;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class DateUtilsTest {
    private static Logger log = LoggerFactory.getLogger(DateUtilsTest.class);

    @Test
    void parseTest() {
        LocalDateTime dateTime = DateUtils.get("2018-11-21T22:31:51.000000");
        assertEquals(2018, dateTime.getYear());
        assertEquals(11, dateTime.getMonthValue());
        assertEquals(21, dateTime.getDayOfMonth());
        assertEquals(22, dateTime.getHour());
        assertEquals(31, dateTime.getMinute());
        assertEquals(51, dateTime.getSecond());
    }
}
