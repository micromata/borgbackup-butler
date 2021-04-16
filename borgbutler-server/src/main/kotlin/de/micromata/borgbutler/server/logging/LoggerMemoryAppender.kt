package de.micromata.borgbutler.server.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import java.util.*

private val log = KotlinLogging.logger {}

class LoggerMemoryAppender : AppenderBase<ILoggingEvent?>() {
    private var lastLogEntryOrderNumber = -1

    var queue = FiFoBuffer<LoggingEventData>(QUEUE_SIZE)

    override fun append(event: ILoggingEvent?) {
        val eventData = LoggingEventData(event)
        eventData.orderNumber = ++lastLogEntryOrderNumber
        queue.add(eventData)
    }

    /**
     * For testing purposes.
     *
     * @param event
     */
    fun append(event: LoggingEventData) {
        queue.add(event)
    }

    fun query(filter: LogFilter?, locale: Locale?): List<LoggingEventData> {
        val result: MutableList<LoggingEventData> = ArrayList()
        if (filter == null) {
            return result
        }
        var maxSize = if (filter.maxSize != null) filter.maxSize else MAX_RESULT_SIZE
        if (maxSize > MAX_RESULT_SIZE) {
            maxSize = MAX_RESULT_SIZE
        }
        var counter = 0
        //I18n i18n = CoreI18n.getDefault().get(locale);
        if (filter.isAscendingOrder) {
            for (i in 0 until queue.size) {
                val resultEvent = getResultEvent(filter, queue[i], locale) ?: continue
                result.add(resultEvent)
                if (++counter > maxSize) break
            }
        } else {
            for (i in queue.size downTo 0) {
                val resultEvent = getResultEvent(filter, queue[i], locale) ?: continue
                result.add(resultEvent)
                if (++counter > maxSize) break
            }
        }
        return result
    }

    private fun getResultEvent(filter: LogFilter, event: LoggingEventData?, locale: Locale?): LoggingEventData? {
        if (event == null) {
            return null
        }
        if (!event.getLevel().matches(filter.threshold)) {
            return null
        }
        if (filter.lastReceivedLogOrderNumber != null) {
            if (event.getOrderNumber() <= filter.lastReceivedLogOrderNumber) {
                return null
            }
        }
        var logString: String? = null
        val message = event.getMessage()
        val localizedMessage = false
        /*if (message != null && message.startsWith("i18n=")) {
                I18nLogEntry i18nLogEntry = I18nLogEntry.parse(message);
                message = i18n.formatMessage(i18nLogEntry.getI18nKey(), (Object[])i18nLogEntry.getArgs());
                localizedMessage = true;
            }*/if (StringUtils.isNotBlank(filter.search)) {
            val sb = StringBuilder()
            sb.append(event.logDate)
            append(sb, event.getLevel(), true)
            append(sb, message, true)
            append(sb, event.getJavaClass(), true)
            append(sb, event.stackTrace, filter.isShowStackTraces)
            logString = sb.toString()
        }
        if (logString == null || matches(logString, filter.search)) {
            var resultEvent: LoggingEventData = event
            if (localizedMessage) {
                // Need a clone
                resultEvent = event.clone()
                resultEvent.setMessage(message)
            }
            return resultEvent
        }
        return null
    }

    private fun append(sb: StringBuilder, value: Any?, append: Boolean) {
        if (!append || value == null) {
            return
        }
        sb.append("|#|").append(value)
    }

    private fun matches(str: String, searchString: String): Boolean {
        if (StringUtils.isBlank(str)) {
            return StringUtils.isBlank(searchString)
        }
        return if (StringUtils.isBlank(searchString)) {
            true
        } else str.toLowerCase().contains(searchString.toLowerCase())
    }

    companion object {
        private const val MAX_RESULT_SIZE = 1000
        private const val QUEUE_SIZE = 10000
        private var instance: LoggerMemoryAppender? = null

        fun getInstance(): LoggerMemoryAppender {
            return instance!!
        }
    }

    /**
     * Initialized by logback on start-up (see logback-spring.xml).
     */
    init {
        if (instance != null) {
            log.warn { "*** LoggerMemoryAppender instantiated twice! Shouldn't occur. ***" }
        } else {
            instance = this
        }
    }
}
