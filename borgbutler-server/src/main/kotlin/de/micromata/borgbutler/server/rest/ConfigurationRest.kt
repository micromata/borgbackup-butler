package de.micromata.borgbutler.server.rest

import de.micromata.borgbutler.cache.ButlerCache
import de.micromata.borgbutler.config.ConfigurationHandler
import de.micromata.borgbutler.json.JsonUtils
import de.micromata.borgbutler.server.BorgInstallation
import de.micromata.borgbutler.server.ServerConfiguration
import de.micromata.borgbutler.server.user.UserData
import de.micromata.borgbutler.server.user.UserManager
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.springframework.web.bind.annotation.*

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/rest/configuration")
class ConfigurationRest {

    /**
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @see JsonUtils.toJson
     */
    @GetMapping("config")
    fun getConfig(@RequestParam("prettyPrinter", required = false) prettyPrinter: Boolean?): String {
        val configurationInfo = ConfigurationInfo()
        configurationInfo.serverConfiguration = ServerConfiguration.get()
        configurationInfo.borgVersion = BorgInstallation.getInstance().getBorgVersion()
        return JsonUtils.toJson(configurationInfo, prettyPrinter)
    }

    @PostMapping("config")
    fun setConfig(@RequestBody configurationInfo: ConfigurationInfo) {
        val configurationHandler = ConfigurationHandler.getInstance()
        BorgInstallation.getInstance()
            .configure(configurationInfo.serverConfiguration, configurationInfo.borgVersion?.borgBinary)
        val configuration: ServerConfiguration = ServerConfiguration.get()
        configurationInfo.serverConfiguration?.let {
            configuration.copyFrom(it)
        }
        configurationHandler.save()
    }

    /**
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @see JsonUtils.toJson
     */
    @GetMapping("user")
    fun getUser(@RequestParam("prettyPrinter", required = false) prettyPrinter: Boolean?): String {
        val user: UserData = RestUtils.getUser()
        return JsonUtils.toJson(user, prettyPrinter)
    }

    @PostMapping("user")
    fun setUser(@RequestBody user: UserData) {
        if (user.getLocale() != null && StringUtils.isBlank(user.getLocale().getLanguage())) {
            // Don't set locale with "" as language.
            user.setLocale(null)
        }
        if (StringUtils.isBlank(user.getDateFormat())) {
            // Don't set dateFormat as "".
            user.setDateFormat(null)
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
