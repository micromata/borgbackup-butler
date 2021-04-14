package org.micromata.borgbutler.config

import de.micromata.borgbutler.config.Definitions
import de.micromata.borgbutler.json.JsonUtils
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private val log = KotlinLogging.logger {}

/**
 * Legacy functionality for older BorgButler installations.
 */
object Legacy  {
    /**
     * Backward compability
     */
    fun readOldJsonConfigFile(workingDir: File, jsonFilename: String): Configuration? {
        val jsonConfigFile = File(workingDir, jsonFilename)
        if (!jsonConfigFile.canRead()) {
            // Nothing to do
            return null
        }
        var json: String? = null
        if (jsonConfigFile.exists()) {
            json = FileUtils.readFileToString(jsonConfigFile, Definitions.STD_CHARSET)
            // Migrate from first version:
            if (json.contains("repo-configs")) {
                json = json.replace("repo-configs", "repoConfigs")
                json = json.replace("display_name", "displayName")
            }
            val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
            val backupFilename = "${formatter.format(Date())}-old-${jsonConfigFile.name}"
            log.info("Migrating old json config file to yaml file. Renaming old json file '${jsonConfigFile.absolutePath}' to '$backupFilename'.")
            FileUtils.moveFile(jsonConfigFile, File(jsonConfigFile.parent, backupFilename))
        }
        return JsonUtils.fromJson(ConfigurationHandler.getConfigClazz(), json)
    }
}
