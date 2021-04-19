package de.micromata.borgbutler.config

import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private val log = KotlinLogging.logger {}

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
        configuration = readJsonConfigfile(workingDir)
        if (configuration == null) {
            Legacy.readOldJsonConfigFile(workingDir, OLD_JSON_CONFIG_FILENAME)?.let {
                configuration = it
                save()
            }
        }
        try {
            if (configuration == null) {
                try {
                    configuration = configClazz.getDeclaredConstructor().newInstance()
                    save()
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

    companion object {
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
        fun getInstance(): ConfigurationHandler {
            if (instance == null) instance = ConfigurationHandler()
            return instance!!
        }

        @kotlin.jvm.JvmStatic
        fun getConfiguration(): Configuration {
            return getInstance().configuration!!
        }

        @kotlin.jvm.JvmStatic
        fun setConfigClazz(configClazz: Class<out Configuration>) {
            Companion.configClazz = configClazz
        }

        @kotlin.jvm.JvmStatic
        fun getConfigClazz(): Class<out Configuration> {
            return configClazz
        }

        fun readJsonConfigfile(workingDir: File): Configuration? {
            val configFile = File(workingDir, CONFIG_FILENAME)
            if (!configFile.canRead()) {
                return null
            }
            log.info("Reading config file '" + configFile.absolutePath + "'")
            val yaml = FileUtils.readFileToString(configFile, Definitions.STD_CHARSET)
            return YamlUtils.fromYaml(configClazz, yaml)
        }
    }
}
