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

package de.micromata.borgbutler.server.rest

import mu.KotlinLogging
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import javax.servlet.http.HttpServletRequest

private val log = KotlinLogging.logger {}

@ControllerAdvice
internal class GlobalDefaultExceptionHandler {
    @ExceptionHandler(value = [(Exception::class)])
    @Throws(Exception::class)
    fun defaultErrorHandler(request: HttpServletRequest, ex: Exception): Any {
        // If the exception is annotated with @ResponseStatus rethrow it and let
        // the framework handle it.
        if (AnnotationUtils.findAnnotation(ex.javaClass, ResponseStatus::class.java) != null) {
            throw ex
        }
        log.error(
            "Exception while processing request: ${ex.message} Request: ${RequestLog.asJson(request)},\nexception=${ex.message}\n${
                ExceptionStackTracePrinter.toString(
                    ex,
                    false
                )
            }"
        )
        return ResponseEntity("Internal error.", HttpStatus.BAD_REQUEST)
    }
}
