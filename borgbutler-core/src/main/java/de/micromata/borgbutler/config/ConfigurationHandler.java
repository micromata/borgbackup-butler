package de.micromata.borgbutler.config;

import de.micromata.borgbutler.json.JsonUtils;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class ConfigurationHandler {
    private static Logger log = LoggerFactory.getLogger(ConfigurationHandler.class);
    private static ConfigurationHandler instance = new ConfigurationHandler();
    private static final String CONFIG_FILENAME = ".borgbutler.json";
    private static final String CONFIG_BACKUP_FILENAME = ".borgbutler-bak.json";
    @Getter
    private File configFile;
    private File backupConfigFile;
    private Configuration configuration = new Configuration();

    public static ConfigurationHandler getInstance() {
        return instance;
    }

    public static Configuration getConfiguration() {
        return instance.configuration;
    }

    public void read() {
        log.info("Reading config file '" + configFile.getAbsolutePath() + "'");
        try {
            String json = FileUtils.readFileToString(configFile, Definitions.STD_CHARSET);
            this.configuration = JsonUtils.fromJson(Configuration.class, json);
        } catch (IOException ex) {
            log.error("Error while trying to read from config file: " + configFile.getAbsolutePath() + ": " + ex.getMessage(), ex);
            return;
        }
    }

    public void write() {
        String json = JsonUtils.toJson(configuration, true);
        try {
            if (configFile.exists()) {
                // Create backup-file first:
                log.info("Creating backup file first: '" + backupConfigFile.getAbsolutePath() + "'");
                FileUtils.copyFile(configFile, backupConfigFile);
            }
            log.info("Writing config file '" + configFile.getAbsolutePath() + "'");
            FileUtils.write(configFile, json, Definitions.STD_CHARSET);
        } catch (IOException ex) {
            log.error("Error while trying to write config file: " + configFile.getAbsolutePath() + ": " + ex.getMessage(), ex);
        }
    }

    private ConfigurationHandler() {
        configFile = new File(System.getProperty("user.home"), CONFIG_FILENAME);
        backupConfigFile = new File(System.getProperty("user.home"), CONFIG_BACKUP_FILENAME);
    }
}
