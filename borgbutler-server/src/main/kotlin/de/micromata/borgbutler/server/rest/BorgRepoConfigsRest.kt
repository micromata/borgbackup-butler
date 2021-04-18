package de.micromata.borgbutler.server.rest

import de.micromata.borgbutler.BorgCommandResult
import de.micromata.borgbutler.BorgCommands
import de.micromata.borgbutler.cache.ButlerCache
import de.micromata.borgbutler.config.BorgRepoConfig
import de.micromata.borgbutler.config.ConfigurationHandler
import de.micromata.borgbutler.data.Repository
import de.micromata.borgbutler.jobs.JobResult
import de.micromata.borgbutler.json.JsonUtils
import mu.KotlinLogging
import org.springframework.web.bind.annotation.*

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/rest/repoConfig")
class BorgRepoConfigsRest {
    /**
     * @param id            id or name of repo.
     * @return [BorgRepoConfig] as json string.
     * @see JsonUtils.toJson
     */
    @GetMapping
    fun getRepoConfig(
        @RequestParam("id") id: String
    ): BorgRepoConfig? {
        return ConfigurationHandler.getConfiguration().getRepoConfig(id)
    }

    @PostMapping
    fun setRepoConfig(@RequestBody newRepoConfig: BorgRepoConfig) {
        if ("new" == newRepoConfig.getId()) {
            newRepoConfig.setId(null)
            ConfigurationHandler.getConfiguration().add(newRepoConfig)
        } else if ("init" == newRepoConfig.getId()) {
            newRepoConfig.setId(null)
            ConfigurationHandler.getConfiguration().add(newRepoConfig)
        } else {
            val repoConfig: BorgRepoConfig? =
                ConfigurationHandler.getConfiguration().getRepoConfig(newRepoConfig.getId())
            if (repoConfig == null) {
                log.error("Can't find repo config '" + newRepoConfig.getId() + "'. Can't save new settings.")
                return
            }
            ButlerCache.getInstance().clearRepoCacheAccess(repoConfig.getRepo())
            ButlerCache.getInstance().clearRepoCacheAccess(newRepoConfig.getRepo())
            repoConfig.copyFrom(newRepoConfig)
        }
        ConfigurationHandler.getInstance().save()
    }

    /**
     * @param idOrName id or name of repo to remove from BorgButler.
     * @return "OK" if removed or error string.
     */
    @GetMapping("remove")
    fun removeRepoConfig(@RequestParam("id") idOrName: String): String {
        val result: Boolean = ConfigurationHandler.getConfiguration().remove(idOrName)
        if (!result) {
            val error = "Repo config with id or name '$idOrName' not found. Can't remove the repo."
            log.error(error)
            return error
        }
        ConfigurationHandler.getInstance().save()
        return "OK"
    }

    /**
     * @param jsonRepoConfig All configuration value of the repo to check.
     * @return Result of borg (tbd.).
     */
    @PostMapping("check")
    fun checkConfig(@RequestBody repoConfig: BorgRepoConfig): String {
        log.info("Testing repo config: $repoConfig")
        val result: BorgCommandResult<Repository> = BorgCommands.info(repoConfig)
        return if (result.getStatus() == JobResult.Status.OK) "OK" else result.getError()
    }
}
