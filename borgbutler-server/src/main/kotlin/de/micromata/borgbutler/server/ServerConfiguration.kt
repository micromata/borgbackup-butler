package de.micromata.borgbutler.server

import de.micromata.borgbutler.config.Configuration
import de.micromata.borgbutler.config.ConfigurationHandler.Companion.getConfiguration
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

class ServerConfiguration : Configuration() {
    var port = WEBSERVER_PORT_DEFAULT

    /**
     * If true, CrossOriginFilter will be set.
     */
    var webDevelopmentMode = WEB_DEVELOPMENT_MODE_PREF_DEFAULT

    fun copyFrom(other: ServerConfiguration) {
        super.copyFrom(other)
        port = other.port
        webDevelopmentMode = other.webDevelopmentMode
    }

    companion object {
        val supportedLanguages = arrayOf("en", "de")
        const val WEBSERVER_PORT_DEFAULT = 9042
        private const val WEB_DEVELOPMENT_MODE_PREF_DEFAULT = false

        @JvmStatic
        fun get(): ServerConfiguration {
            return getConfiguration() as ServerConfiguration
        }
    }
}
