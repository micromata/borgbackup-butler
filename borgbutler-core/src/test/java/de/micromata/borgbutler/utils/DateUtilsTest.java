package de.micromata.borgbutler.utils;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class DateUtilsTest {
    private static Logger log = LoggerFactory.getLogger(DateUtilsTest.class);

    @Test
    void parseTest() {
        assertEquals("2018-11-21 22:31:51",DateUtils.format("2018-11-21T22:31:51.000000"));
    }
}
