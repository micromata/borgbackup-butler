/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2021 Micromata GmbH, Germany (www.micromata.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package de.micromata.borgbutler

import mu.KLogger
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils

/**
 * Helper for logging very important information and warnings.
 */
class EmphasizedLogSupport @JvmOverloads constructor(
    private val log: KLogger,
    priority: Priority? = Priority.IMPORTANT,
    private val alignment: Alignment = Alignment.CENTER
) {
    private var number = 0
    private val innerLength: Int
    var logLevel = LogLevel.INFO
    private var started = false

    constructor(log: KLogger, alignment: Alignment= Alignment.CENTER) : this(log, Priority.IMPORTANT, alignment)

    enum class Priority {
        NORMAL, IMPORTANT, VERY_IMPORTANT
    }

    enum class Alignment {
        CENTER, LEFT
    }

    enum class LogLevel {
        ERROR, WARN, INFO
    }

    private fun ensureStart() {
        if (!started) {
            started = true
            logStartSeparator()
        }
    }

    /**
     * @return this for chaining.
     */
    private fun logStartSeparator(): EmphasizedLogSupport {
        for (i in 0 until number) {
            logSeparatorLine()
        }
        return log("")
    }

    /**
     * @return this for chaining.
     */
    fun logEnd(): EmphasizedLogSupport {
        ensureStart()
        log("")
        for (i in 0 until number) {
            logSeparatorLine()
        }
        return this
    }

    private fun logSeparatorLine() {
        logLine(StringUtils.rightPad("", innerLength, '*') + asterisks(number * 2 + 2))
    }

    fun log(text: String?): EmphasizedLogSupport {
        ensureStart()
        if (text?.contains("\n") == true) {
            for (line in StringUtils.splitPreserveAllTokens(text, '\n')) {
                logLineText(line)
            }
        } else {
            logLineText(text)
        }
        return this
    }

    private fun logLineText(line: String?) {
        val padText = if (alignment == Alignment.LEFT)
            StringUtils.rightPad(line, innerLength)
        else
            StringUtils.center(line, innerLength)
        logLine(asterisks(number) + " " + padText + " " + asterisks(number))
    }

    private fun logLine(msg: String) {
        if (logLevel == LogLevel.ERROR) log.error(msg) else if (logLevel == LogLevel.WARN) log.warn(msg) else log.info(
            msg
        )
    }

    companion object {
        private const val CONSOLE_LENGTH = 120
        private fun asterisks(number: Int): String {
            return StringUtils.rightPad("*", number, '*')
        }
    }

    init {
        when (priority) {
            Priority.NORMAL -> number = 1
            Priority.VERY_IMPORTANT -> {
                number = 5
                logLevel = LogLevel.WARN
            }
            else -> number = 2
        }
        innerLength = CONSOLE_LENGTH - 2 * number
    }
}
