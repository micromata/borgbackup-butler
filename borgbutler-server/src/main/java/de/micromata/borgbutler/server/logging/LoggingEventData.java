package de.micromata.borgbutler.server.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import org.apache.commons.lang3.ClassUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * For easier serialization: JSON
 */
public class LoggingEventData implements Cloneable {
    private SimpleDateFormat ISO_DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    int orderNumber;
    LogLevel level;
    String message;
    private String messageObjectClass;
    private String loggerName;
    private String logDate;
    String javaClass;
    private String javaClassSimpleName;
    private int lineNumber;
    private String methodName;
    private String stackTrace;

    LoggingEventData() {
    }

    public LoggingEventData(ILoggingEvent event) {
        level = LogLevel.getLevel(event);
        message = event.getFormattedMessage();
        messageObjectClass = event.getMessage().getClass().toString();
        loggerName = event.getLoggerName();
        logDate = getIsoLogDate(event.getTimeStamp());
        StackTraceElement info = event.getCallerData()[0];
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            printWriter.append(ThrowableProxyUtil.asString(throwableProxy));
            printWriter.append(CoreConstants.LINE_SEPARATOR);
            stackTrace = writer.toString();
        }
        if (info != null) {
            javaClass = info.getClassName();
            javaClassSimpleName = ClassUtils.getShortClassName(info.getClassName());
            lineNumber = info.getLineNumber();
            methodName = info.getMethodName();
        }
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageObjectClass() {
        return messageObjectClass;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public String getLogDate() {
        return logDate;
    }

    public String getJavaClass() {
        return javaClass;
    }

    public String getJavaClassSimpleName() {
        return javaClassSimpleName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String getIsoLogDate(long millis) {
        synchronized (ISO_DATEFORMAT) {
            return ISO_DATEFORMAT.format(new Date(millis));
        }
    }

    @Override
    public LoggingEventData clone() {
        LoggingEventData clone = null;
        try {
            clone = (LoggingEventData) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new UnsupportedOperationException(this.getClass().getCanonicalName() + " isn't cloneable: " + ex.getMessage(), ex);
        }
        return clone;
    }
}
