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

import de.micromata.borgbutler.json.JsonUtils
import java.security.Principal
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.Part

/**
 * Helper class for debugging requests. Converts a given request to json.
 */
object RequestLog {
    @JvmStatic
    fun asJson(request: HttpServletRequest, longForm: Boolean = false): String {
        val data = RequestData(request, longForm)
        return JsonUtils.toJson(data)
    }

    @JvmStatic
    fun asString(request: HttpServletRequest): String {
        return request.requestURI
    }
}

class RequestData(request: HttpServletRequest, longForm: Boolean = false) {
    val attributes = mutableMapOf<String, Any?>()
    val parameters = mutableMapOf<String, String?>()
    val headers = mutableMapOf<String, String?>()
    var locales: MutableList<Locale>? = null
    //val parts = mutableListOf<PartInfo>()

    val authType: String? = request.authType
    val characterEncoding: String? = request.characterEncoding
    val contentLength: Int? = if (longForm) request.contentLength else null
    val contentType: String? = if (longForm) request.contentType else null
    val contextPath: String? = if (longForm) request.contextPath else null
    val cookies: Array<Cookie>? = if (longForm) request.cookies else null
    val isAsyncStarted: Boolean? = if (longForm) request.isAsyncStarted else null
    val isRequestedSessionIdFromCookie: Boolean? = if (longForm) request.isRequestedSessionIdFromCookie else null
    val isRequestedSessionIdFromURL: Boolean? = if (longForm) request.isRequestedSessionIdFromURL else null
    val isRequestedSessionIdValid: Boolean? = if (longForm) request.isRequestedSessionIdValid else null
    val isSecure: Boolean? = if (longForm) request.isSecure else null
    val isTrailerFieldsReady: Boolean? = if (longForm) request.isTrailerFieldsReady else null
    val localAddr: String? = if (longForm) request.localAddr else null
    val localName: String? = if (longForm) request.localName else null
    val localPort: Int? = if (longForm) request.localPort else null
    val locale: Locale? = if (longForm) request.locale else null
    val method: String? = request.method
    val pathInfo: String? = if (longForm) request.pathInfo else null
    val pathTranslated: String? = if (longForm) request.pathTranslated else null
    val protocol: String? = if (longForm) request.protocol else null
    val queryString: String? = request.queryString
    val remoteAddr: String? = request.remoteAddr
    val remoteHost: String? = if (longForm) request.remoteHost else null
    val remotePort: Int? = if (longForm) request.remotePort else null
    val remoteUser: String? = request.remoteUser
    val requestedSessionId: String? = if (longForm) request.requestedSessionId else null
    val requestUri: String? = request.requestURI
    val scheme: String? = if (longForm) request.scheme else null
    val serverName = if (longForm) request.serverName else null
    val serverPort: Int? = if (longForm) request.serverPort else null
    val servletPath: String? = if (longForm) request.servletPath else null
    val sessionId: String? = request.session?.id
    val userPrincipal: Principal? = request.userPrincipal


    init {
        for (attribute in request.attributeNames) {
            attributes[attribute] = handleSecret(request, attribute, request.getAttribute(attribute)?.toString())
        }
        for (header in request.headerNames) {
            headers[header] = handleSecret(request, header, request.getHeader(header)) as String
        }
        if (longForm) {
            locales = mutableListOf()
            locales?.let {
                for (locale in request.locales) {
                    it.add(locale)
                }
            }
        }
        for (parameter in request.parameterNames) {
            parameters[parameter] = handleSecret(request, parameter, request.getParameter(parameter)) as String
        }
        /*
        for (part in request.parts) {
            parts.add(PartInfo(part))
        }*/
    }

    private fun <T> handleSecret(request: HttpServletRequest, name: String?, value: T?): Any? {
        name ?: return null
        value ?: return null
        if (name.toLowerCase() == "authorization") {
            return "authorization=******"
        }
        return value
    }
}

class PartInfo(part: Part) {
    val headers = mutableMapOf<String, String>()
    // Can't get inputstream

    val contentType: String? = part.contentType
    val name: String? = part.name
    val size = part.size

    init {
        for (header in part.headerNames) {
            headers[header] = part.getHeader(header)
        }
    }
}
