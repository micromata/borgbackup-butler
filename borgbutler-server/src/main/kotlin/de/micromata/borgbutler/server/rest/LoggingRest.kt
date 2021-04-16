package de.micromata.borgbutler.server.rest

import de.micromata.borgbutler.json.JsonUtils
import de.micromata.borgbutler.server.logging.LoggerMemoryAppender
import de.micromata.borgbutler.server.logging.LogFilter
import de.micromata.borgbutler.server.logging.LogLevel
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/rest/logging")
class LoggingRest {
    /**
     * @param request
     * @param search
     * @param logLevelTreshold fatal, error, warn, info, debug or trace (case insensitive).
     * @param maxSize          Max size of the result list.
     * @param ascendingOrder   Default is false (default is descending order).
     * @param lastReceivedOrderNumber The last received order number for updating log entries (preventing querying all entries again).
     * @param prettyPrinter
     * @return
     */
    @GetMapping("query")
    fun query(
        request: HttpServletRequest?,
        @RequestParam("search", required = false) search: String?,
        @RequestParam("treshold", required = false) logLevelTreshold: String?,
        @RequestParam("maxSize", required = false) maxSize: Int?,
        @RequestParam("ascendingOrder", required = false) ascendingOrder: Boolean?,
        @RequestParam("lastReceivedOrderNumber", required = false) lastReceivedOrderNumber: Int?,
        @RequestParam("prettyPrinter", required = false) prettyPrinter: Boolean?
    ): String {
        val filter = LogFilter()
        filter.setSearch(search)
        if (logLevelTreshold != null) {
            try {
                val treshold =
                    LogLevel.valueOf(logLevelTreshold.trim { it <= ' ' }
                        .toUpperCase())
                filter.setThreshold(treshold)
            } catch (ex: IllegalArgumentException) {
                log.error("Can't parse log level treshold: " + logLevelTreshold + ". Supported values (case insensitive): " + LogLevel.getSupportedValues())
            }
        }
        if (filter.getThreshold() == null) {
            filter.setThreshold(LogLevel.INFO)
        }
        if (maxSize != null) {
            filter.setMaxSize(maxSize)
        }
        if (ascendingOrder != null && ascendingOrder == true) {
            filter.setAscendingOrder(true)
        }
        if (lastReceivedOrderNumber != null) {
            filter.setLastReceivedLogOrderNumber(lastReceivedOrderNumber)
        }
        val loggerMemoryAppender = LoggerMemoryAppender.getInstance()
        return JsonUtils.toJson(loggerMemoryAppender.query(filter, RestUtils.getUserLocale(request!!)), prettyPrinter)
    }
}
