package de.micromata.borgbutler.server.rest

import de.micromata.borgbutler.cache.ButlerCache
import de.micromata.borgbutler.config.ConfigurationHandler
import de.micromata.borgbutler.server.BorgInstallation
import de.micromata.borgbutler.server.ServerConfiguration
import de.micromata.borgbutler.server.user.UserData
import de.micromata.borgbutler.server.user.UserManager
import mu.KotlinLogging
import org.springframework.web.bind.annotation.*

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/rest/configuration")
class ConfigurationRest {

    @GetMapping("config")
    fun getConfig(): ConfigurationInfo {
        val configurationInfo = ConfigurationInfo()
        configurationInfo.serverConfiguration = ServerConfiguration.get()
        configurationInfo.borgConfig = BorgInstallation.getInstance().borgConfig
        return configurationInfo
    }

    @PostMapping("config")
    fun setConfig(@RequestBody configurationInfo: ConfigurationInfo) {
        log.info("server-config: ${configurationInfo.serverConfiguration}, borg-binary: ${configurationInfo.borgConfig?.borgBinary}")
        BorgInstallation.getInstance().configure(configurationInfo.serverConfiguration, configurationInfo.borgConfig)
        val configuration: ServerConfiguration = ServerConfiguration.get()
        configurationInfo.serverConfiguration?.let {
            configuration.copyFrom(it)
        }
        val configurationHandler = ConfigurationHandler.getInstance()
        configurationHandler.save()
    }

    @GetMapping("user")
    fun getUser(): UserData {
        return RestUtils.getUser()
    }

    @PostMapping("user")
    fun setUser(@RequestBody user: UserData) {
        if (user.locale?.language?.isBlank() == true) {
            // Don't set locale with "" as language.
            user.locale = null
        }
        if (user.dateFormat?.isBlank() == true) {
            // Don't set dateFormat as "".
            user.dateFormat = null
        }
        UserManager.instance().saveUser(user)
    }

    /**
     * Resets the settings to default values (deletes all settings).
     */
    @GetMapping("clearAllCaches")
    fun clearAllCaches(): String {
        log.info("Clear all caches called...")
        ButlerCache.getInstance().clearAllCaches()
        return "OK"
    }
}
