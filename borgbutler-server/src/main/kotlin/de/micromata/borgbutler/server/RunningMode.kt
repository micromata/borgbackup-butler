package de.micromata.borgbutler.server

import org.slf4j.LoggerFactory
import java.awt.Desktop
import java.util.*

object RunningMode {
    private val log = LoggerFactory.getLogger(RunningMode::class.java)
    private var osType: OSType? = null
    var webDevelopment: Boolean = false
        internal set
    val headlessMode: Boolean = System.getProperty("java.awt.headless") == "true"
    val desktopSupported = !headlessMode && Desktop.isDesktopSupported()
    val desktopSupportsBrowse = desktopSupported && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
    @JvmStatic
    val dockerMode = System.getProperty("docker") == "true"

    @JvmStatic
    val userManagement = UserManagement.SINGLE

    @JvmStatic
    val oSType: OSType?
        get() {
            if (osType == null) {
                val osTypeString = System.getProperty("os.name")
                osType = if (osTypeString == null) {
                    OSType.OTHER
                } else if (osTypeString.toLowerCase().contains("mac")) {
                    OSType.MAC_OS
                } else if (osTypeString.toLowerCase().contains("win")) {
                    OSType.WINDOWS
                } else if (osTypeString.toLowerCase().contains("linux")) {
                    OSType.LINUX
                } else if (osTypeString.toLowerCase().contains("freebsd")) {
                    OSType.FREEBSD
                } else {
                    OSType.OTHER
                }
            }
            return osType
        }

    val runningInIDE: Boolean
        get() {
            if (!webBundled()) {
                log.warn("*** BorgButler seems to run inside an IDE. ***")
                return true
            } else {
                return false
            }
        }

    /**
     * After setting all values you should call this method for a logging output with all current settings.
     */
    fun logMode() {
        log.info(
            "Starting ${Version.getInstance().appName} ${Version.getInstance().version} ("
                    + Version.getInstance().formatBuildDateISO(TimeZone.getDefault())
                    + ") with: webDevelopment=$webDevelopment, desktopSupported=$desktopSupported, javaVersion='"
                    + System.getProperty("java.version") + "'."
        )
    }

    fun webBundled(): Boolean {
        var available = false
        RunningMode::class.java.getResourceAsStream("/webapp/index.html")?.use {
        //RunningMode::class.java.getResourceAsStream("/logback-spring.xml")?.use {
            log.info("Web app found (is bundled).")
            available = true
        }
        if (!available) {
            log.info("Web app isn't bundled (running in IDE?)")
        }
        return available
    }

    enum class UserManagement {
        SINGLE
    }

    enum class OSType {
        MAC_OS, WINDOWS, LINUX, FREEBSD, OTHER
    }
}
