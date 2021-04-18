package de.micromata.borgbutler.server.rest

import de.micromata.borgbutler.server.RunningMode
import de.micromata.borgbutler.server.user.UserData
import de.micromata.borgbutler.server.user.UserUtils
import org.slf4j.Logger
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.io.InputStream
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*
import javax.servlet.ServletRequest
import javax.servlet.http.HttpServletRequest

object RestUtils {
    /**
     * @return null, if the local app (JavaFX) is running and the request is from localhost. Otherwise message, why local
     * service isn't available.
     */
    fun checkLocalDesktopAvailable(requestContext: HttpServletRequest): String? {
        if (!RunningMode.desktopSupported) {
            return "Service unavailable. No desktop app on localhost available."
        }
        val remoteAddr = requestContext.remoteAddr
        return if (remoteAddr == null || remoteAddr != "127.0.0.1") {
            "Service not available. Can't call this service remote. Run this service on localhost of the running desktop app."
        } else null
    }

    /**
     * @return Returns the user put by the UserFilter.
     * @see UserUtils.getUser
     * @see de.micromata.borgbutler.server.user.UserFilter
     */
    fun getUser(): UserData {
        val user = UserUtils.getUser() ?: throw IllegalStateException("No user given in rest call.")
        return UserUtils.getUser()
    }

    fun getUserLocale(requestContext: HttpServletRequest): Locale? {
        val user = getUser()
        var locale = user.locale
        if (locale == null) {
            locale = requestContext.locale
        }
        return locale
    }

    @JvmStatic
    fun getClientIp(request: ServletRequest): String? {
        var remoteAddr: String? = null
        if (request is HttpServletRequest) {
            remoteAddr = request.getHeader("X-Forwarded-For")
        }
        if (remoteAddr != null) {
            if (remoteAddr.contains(",")) {
                // sometimes the header is of form client ip,proxy 1 ip,proxy 2 ip,...,proxy n ip,
                // we just want the client
                remoteAddr = remoteAddr.split(',')[0].trim { it <= ' ' }
            }
            try {
                // If ip4/6 address string handed over, simply does pattern validation.
                InetAddress.getByName(remoteAddr)
            } catch (e: UnknownHostException) {
                remoteAddr = request.remoteAddr
            }

        } else {
            remoteAddr = request.remoteAddr
        }
        return remoteAddr
    }

    fun downloadFile(filename: String, inputStream: InputStream): ResponseEntity<InputStreamResource> {
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/octet-stream"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${filename.replace('"', '_')}\"")
            .body(InputStreamResource(inputStream))
    }

    fun downloadFile(filename: String, content: String): ResponseEntity<String> {
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/octet-stream"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${filename.replace('"', '_')}\"")
            .body(content)
    }

    fun downloadFile(filename: String, resource: ByteArrayResource): ResponseEntity<Resource> {
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/octet-stream"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${filename.replace('"', '_')}\"")
            .body(resource)
    }

    fun badRequest(message: String): ResponseEntity<String> {
        return ResponseEntity.badRequest().body(message)
    }

    fun notFound(): ResponseEntity<String> {
        return ResponseEntity.badRequest().body("Not found.")
    }

    fun notFound(log: Logger, errorMessage: String?): ResponseEntity<String> {
        log.error(errorMessage)
        return ResponseEntity.badRequest().body(errorMessage)
    }
}
