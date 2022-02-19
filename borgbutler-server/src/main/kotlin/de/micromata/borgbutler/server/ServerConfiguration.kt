package de.micromata.borgbutler.server

import de.micromata.borgbutler.config.Configuration
import de.micromata.borgbutler.config.ConfigurationHandler.Companion.getConfiguration
import mu.KotlinLogging

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

    override fun toString(): String {
        return "${super.toString()}, port=[$port], webDevelopmentMode=[$webDevelopmentMode]"
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
