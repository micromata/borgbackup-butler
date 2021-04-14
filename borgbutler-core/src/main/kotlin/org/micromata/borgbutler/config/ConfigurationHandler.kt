package org.micromata.borgbutler.config

import de.micromata.borgbutler.config.Definitions
import de.micromata.borgbutler.json.JsonUtils
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Reads and writes config file borgbutler-config.json/borgbutler-config.yaml
 */
class ConfigurationHandler private constructor(butlerHomeDir: String? = null) {
    val configFile: File
    private val configBackupDir: File
    var workingDir: File
        private set
    private val butlerHomeDir: File? = null
    private var configuration: Configuration? = null

    private fun read() {
        if (configFile.canRead()) {
            log.info("Reading config file '" + configFile.absolutePath + "'")
        } else {
            readOldJson()
        }
        try {
            if (configuration == null) {
                try {
                    configuration = configClazz.getDeclaredConstructor().newInstance()
                } catch (ex: Exception) {
                    log.error(
                        "Internal error: Can't instantiate object of type '" + configClazz + "': " + ex.message,
                        ex
                    )
                    return
                }
            }
            configuration?.getRepoConfigs()?.filter { it.displayName.isNullOrBlank() }?.forEach { repoConfig ->
                repoConfig.displayName = repoConfig.repo
            }
            configuration?.setWorkingDir(workingDir)
        } catch (ex: IOException) {
            log.error("Error while trying to read from config file: " + configFile.absolutePath + ": " + ex.message, ex)
            return
        }
    }

    /**
     * Backward compability
     */
    private fun readOldJson() {
        val jsonConfigFile = File(workingDir, OLD_JSON_CONFIG_FILENAME)
        if (!jsonConfigFile.canRead()) {
            // Nothing to do
            return
        }
        var json: String? = null
        if (jsonConfigFile.exists()) {
            json = FileUtils.readFileToString(jsonConfigFile, Definitions.STD_CHARSET)
            // Migrate from first version:
            if (json.contains("repo-configs")) {
                json = json.replace("repo-configs", "repoConfigs")
                json = json.replace("display_name", "displayName")
            }
            val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss'-'")
            val backupFilename = "${formatter.format(Date())}-old-${jsonConfigFile.name}"
            log.info("Migrating old json config file to yaml file. Renaming old json file '${jsonConfigFile.absolutePath}' to '$backupFilename'.")
            FileUtils.moveFile(jsonConfigFile, File(jsonConfigFile.parent, backupFilename))
        }
        val newConfig = JsonUtils.fromJson(configClazz, json)
            ?: // Nothing to do
            return
        configuration = newConfig
        save()
    }

    fun save() {
        configuration?.getRepoConfigs()?.filter { !it.passphrase.isNullOrBlank() }?.forEach { repoConfig ->
            log.info("Removing password command from config because password command is given: " + repoConfig.passwordCommand)
            repoConfig.passphrase = null // Don't use password (anymore) if password command is available.
        }
        val yaml = YamlUtils.toYaml(configuration)
        try {
            if (configFile.exists()) {
                // Create backup-file first:
                makeBackup(configFile)
            }
            log.info("Writing config file '" + configFile.absolutePath + "'")
            FileUtils.write(configFile, yaml, Definitions.STD_CHARSET)
        } catch (ex: IOException) {
            log.error("Error while trying to write config file: " + configFile.absolutePath + ": " + ex.message, ex)
        }
    }

    private fun makeBackup(file: File) {
        val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss'-'")
        val backupFile = File(configBackupDir, formatter.format(Date()) + file.name)
        log.info("Creating backup file first: '" + backupFile.absolutePath + "'")
        FileUtils.copyFile(file, backupFile)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ConfigurationHandler::class.java)
        private var instance: ConfigurationHandler? = null
        private const val BUTLER_HOME_DIR = ".borgbutler"
        private const val OLD_JSON_CONFIG_FILENAME = "borgbutler-config.json"
        private const val CONFIG_FILENAME = "borgbutler.config"
        private const val CONFIG_BACKUP_DIR = "backup"
        private var configClazz: Class<out Configuration> = Configuration::class.java

        @kotlin.jvm.JvmStatic
        fun init(butlerHomeDir: String?) {
            if (instance != null) {
                throw RuntimeException("ConfigurationHandler already initialized")
            }
            instance = ConfigurationHandler(butlerHomeDir)
        }

        @kotlin.jvm.JvmStatic
        fun getInstance(): ConfigurationHandler? {
            if (instance == null) instance = ConfigurationHandler()
            return instance
        }

        @kotlin.jvm.JvmStatic
        fun getConfiguration(): Configuration? {
            return getInstance()!!.configuration
        }

        @kotlin.jvm.JvmStatic
        fun setConfigClazz(configClazz: Class<out Configuration>) {
            Companion.configClazz = configClazz
        }
    }

    init {
        workingDir = if (butlerHomeDir != null) {
            File(butlerHomeDir)
        } else {
            File(System.getProperty("user.home"), BUTLER_HOME_DIR)
        }
        log.info("Using directory '" + workingDir.getAbsolutePath() + "' as BorgButler's home directory.")
        if (!workingDir.exists()) {
            log.info("Creating borg-butlers working directory: " + workingDir.getAbsolutePath())
            workingDir.mkdirs()
        }
        configFile = File(workingDir, CONFIG_FILENAME)
        configBackupDir = File(workingDir, CONFIG_BACKUP_DIR)
        if (!configBackupDir.exists()) {
            log.info("Creating borg-butlers backup directory: " + configBackupDir.absolutePath)
            configBackupDir.mkdirs()
        }
        read()
    }
}