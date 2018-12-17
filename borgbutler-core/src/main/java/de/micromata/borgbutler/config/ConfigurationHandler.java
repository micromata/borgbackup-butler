package de.micromata.borgbutler.config;

import de.micromata.borgbutler.json.JsonUtils;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class ConfigurationHandler {
    private static Logger log = LoggerFactory.getLogger(ConfigurationHandler.class);
    private static ConfigurationHandler instance = new ConfigurationHandler();
    private static final String BUTLER_HOME_DIR = ".borgbutler";
    private static final String CONFIG_FILENAME = "borgbutler-config.json";
    private static final String CONFIG_BACKUP_FILENAME = "borgbutler-config-bak.json";
    @Getter
    private File configFile;
    private File configBackupFile;
    @Getter
    private File workingDir;
    private Configuration configuration = new Configuration();

    public static ConfigurationHandler getInstance() {
        return instance;
    }

    public static Configuration getConfiguration() {
        return instance.configuration;
    }

    private void read() {
        log.info("Reading config file '" + configFile.getAbsolutePath() + "'");
        try {
            String json = FileUtils.readFileToString(configFile, Definitions.STD_CHARSET);
            this.configuration = JsonUtils.fromJson(Configuration.class, json);
            for (BorgRepoConfig repoConfig : this.configuration.getRepoConfigs()) {
                if (StringUtils.isBlank(repoConfig.getDisplayName())) {
                    repoConfig.setDisplayName(repoConfig.getRepo());
                }
            }
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
                log.info("Creating backup file first: '" + configBackupFile.getAbsolutePath() + "'");
                FileUtils.copyFile(configFile, configBackupFile);
            }
            log.info("Writing config file '" + configFile.getAbsolutePath() + "'");
            FileUtils.write(configFile, json, Definitions.STD_CHARSET);
        } catch (IOException ex) {
            log.error("Error while trying to write config file: " + configFile.getAbsolutePath() + ": " + ex.getMessage(), ex);
        }
    }

    private ConfigurationHandler() {
        File userHome = new File(System.getProperty("user.home"));
        workingDir = new File(userHome, BUTLER_HOME_DIR);
        if (!workingDir.exists()) {
            log.info("Creating borg-butlers working directory: " + workingDir.getAbsolutePath());
            workingDir.mkdirs();
        }
        configFile = new File(workingDir, CONFIG_FILENAME);
        configBackupFile = new File(workingDir, CONFIG_BACKUP_FILENAME);
        read();
    }
}
