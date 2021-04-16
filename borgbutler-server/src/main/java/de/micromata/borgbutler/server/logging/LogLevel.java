package de.micromata.borgbutler.server.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.apache.commons.lang3.StringUtils;

public enum LogLevel {
    ERROR, WARN, INFO, DEBUG, TRACE;

    /**
     * @param treshold
     * @return True, if this log level is equals or higher than given treshold. ERROR is the highest and TRACE the lowest.
     */
    public boolean matches(LogLevel treshold) {
        if (treshold == null) {
            return true;
        }
        return this.ordinal() <= treshold.ordinal();
    }

    public static LogLevel getLevel(ILoggingEvent event) {
        switch (event.getLevel().toInt()) {
            case Level.INFO_INT:
                return LogLevel.INFO;
            case Level.DEBUG_INT:
                return LogLevel.DEBUG;
            case Level.WARN_INT:
                return LogLevel.WARN;
            case Level.TRACE_INT:
                return LogLevel.TRACE;
            default:
                return LogLevel.ERROR;
        }

    }

    public static String getSupportedValues() {
        return StringUtils.join(LogLevel.values(), ", ");
    }
}
