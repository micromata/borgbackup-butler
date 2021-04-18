package de.micromata.borgbutler.server.user

import de.micromata.borgbutler.server.RunningMode.dockerMode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException
import javax.servlet.*

/**
 * Ensuring the user data inside request threads. For now, it's only a simple implementation (no login required).
 * Only the user's (client's) locale is used.
 * <br></br>
 * For requests from remote (not localhost) an exception is thrown due to security reasons.
 */
@Component
class UserFilter : Filter {
    private val log = LoggerFactory.getLogger(UserFilter::class.java)
    override fun init(filterConfig: FilterConfig) {}

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        checkClientIp(request)
        try {
            var userData = UserUtils.getUser()
            if (userData != null) {
                log.warn("****************************************")
                log.warn("***********                   **********")
                log.warn("*********** SECURITY WARNING! **********")
                log.warn("***********                   **********")
                log.warn("*********** Internal error:   **********")
                log.warn("*********** User already set! **********")
                log.warn("***********                   **********")
                log.warn("****************************************")
                log.warn("Don't deliver this app in dev mode due to security reasons!")
                val message =
                    "User already given for this request. Rejecting request due to security reasons. Given user: $userData"
                log.error(message)
                throw IllegalArgumentException(message)
            }
            userData = UserManager.instance().getUser("dummy")
            UserUtils.setUser(userData, request.locale)
            if (log.isDebugEnabled) log.debug("Request for user: $userData")
            //log.info("Request for user: " + userData + ": " + RequestLog.asString((HttpServletRequest) request));
            chain.doFilter(request, response)
        } finally {
            UserUtils.removeUser()
        }
    }

    override fun destroy() {}
    private fun checkClientIp(request: ServletRequest) {
        val remoteAddr = request.remoteAddr
        if (check(remoteAddr)) {
            return
        }
        log.warn("****************************************")
        log.warn("***********                   **********")
        log.warn("*********** SECURITY WARNING! **********")
        log.warn("***********                   **********")
        log.warn("*********** External access:  **********")
        log.warn("*********** $remoteAddr **********")
        log.warn("***********                   **********")
        log.warn("****************************************")
        if (allowedClientIps == null) {
            log.warn("Only access from local host yet supported due to security reasons. You may configure client address ranges by -DallowedClientIps=172.17.0.1 or -DallowedClientIps=172.17.")
        } else {
            log.warn("Only access from local host and '${allowedClientIps.joinToString { it }}' (option -DallowedClientIps) yet supported due to security reasons.")
        }
        log.info("Access denied for client with remote address: $remoteAddr")
        throw RuntimeException("Server is only available for localhost due to security reasons. A remote access is not yet available.")
    }

    internal fun check(remoteAddr: String?): Boolean {
        remoteAddr ?: return false
        if (remoteAddr == "127.0.0.1") {
            return true
        }
        if (dockerMode && remoteAddr.startsWith("172.17.0.")) {
            // Docker host uses ip address 171.17.0
            return true
        }
        return allowedClientIps?.any { remoteAddr.startsWith(it) } == true
    }

    private val allowedClientIps =
        System.getProperty(SYSTEM_PROPERTY_ALLOWED_CLIENT_IPS)?.split(",", ";", ":", " ")
            ?.filter { it.isNotBlank() && it.indexOf('.') > 0 }?.map { it.trim { it <= ' ' } }

    init {
        if (!allowedClientIps.isNullOrEmpty()) {
            log.warn("Configured and allowed client ips are: ${allowedClientIps.joinToString { it }}")
        }
    }

    companion object {
        internal const val SYSTEM_PROPERTY_ALLOWED_CLIENT_IPS = "allowedClientIps"
    }
}
