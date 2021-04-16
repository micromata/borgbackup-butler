package de.micromata.borgbutler.config

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import de.micromata.borgbutler.config.BorgRepoConfig
import de.micromata.borgbutler.demo.DemoRepos
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.io.File

private val log = KotlinLogging.logger {}

/**
 * Representation of ~/.borgbutler/borgbutler-config.json.
 */
open class Configuration {

    @JsonIgnore
    private var workingDir: File? = null
    /**
     * Optional borg command to use.
     */
    /**
     * The path of the borg command to use (optional).
     */
    var borgCommand: String? = null
        private set

    /**
     * The borg version to install from github (optional).
     */
    var borgVersion: String? = null

    /**
     * Default is 100 MB (approximately).
     */
    var maxArchiveContentCacheCapacityMb = 100
        private set
    var isShowDemoRepos = true
        private set

    /**
     * Default is restore inside BorgButler's home dir (~/.borgbutler/restore).
     */
    @JsonProperty("restoreDir")
    val restoreDirPath: String? = null

    @JsonIgnore
    private var restoreHomeDir: File? = null

    @JsonProperty
    private val repoConfigs = mutableListOf<BorgRepoConfig>()

    fun add(repoConfig: BorgRepoConfig) {
        synchronized(repoConfigs) { repoConfigs.add(repoConfig) }
    }

    fun remove(idOrName: String?): Boolean {
        if (idOrName == null) {
            return false
        }
        synchronized(repoConfigs) {
            for (repoConfig in allRepoConfigs) {
                if (StringUtils.equals(idOrName, repoConfig.repo) || StringUtils.equals(idOrName, repoConfig.id)) {
                    repoConfigs.remove(repoConfig)
                    return true
                }
            }
        }
        return false
    }

    fun getRepoConfig(idOrName: String?): BorgRepoConfig? {
        if (idOrName == null) {
            return null
        }
        for (repoConfig in allRepoConfigs) {
            if (StringUtils.equals(idOrName, repoConfig.repo) || StringUtils.equals(idOrName, repoConfig.id)) {
                return repoConfig
            }
        }
        return null
    }

    fun getRestoreHomeDir(): File {
        if (restoreHomeDir == null) {
            restoreDirPath?.let {
                restoreHomeDir = File(it)
            } ?: run {
                restoreHomeDir = File(workingDir, RESTORE_DIRNAME)
            }
            if (!restoreHomeDir!!.exists()) {
                log.info("Creating dir '" + restoreHomeDir?.absolutePath + "' for restoring backup files and directories.")
            }
        }
        return restoreHomeDir!!
    }

    fun copyFrom(other: Configuration) {
        borgCommand = other.borgCommand
        maxArchiveContentCacheCapacityMb = other.maxArchiveContentCacheCapacityMb
        isShowDemoRepos = other.isShowDemoRepos
    }

    @get:JsonIgnore
    val allRepoConfigs: List<BorgRepoConfig>
        get() = DemoRepos.getAllRepos(repoConfigs)

    fun getRepoConfigs(): List<BorgRepoConfig> {
        return repoConfigs
    }

    fun setWorkingDir(workingDir: File?): Configuration {
        this.workingDir = workingDir
        return this
    }

    fun setBorgCommand(borgCommand: String?): Configuration {
        this.borgCommand = borgCommand
        return this
    }

    fun setShowDemoRepos(showDemoRepos: Boolean): Configuration {
        isShowDemoRepos = showDemoRepos
        return this
    }

    companion object {
        /**
         * Default dir name for restoring archives.
         */
        private const val RESTORE_DIRNAME = "restore"
    }
}
