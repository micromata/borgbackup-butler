package de.micromata.borgbutler.config;

import de.micromata.borgbutler.json.JsonUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConfigurationHandler {
    private static Logger log = LoggerFactory.getLogger(ConfigurationHandler.class);
    private static ConfigurationHandler instance;
    private static final String BUTLER_HOME_DIR = ".borgbutler";
    private static final String CONFIG_FILENAME = "borgbutler-config.json";
    private static final String CONFIG_BACKUP_DIR = "backup";
    @Getter
    private File configFile;
    private File configBackupDir;
    @Getter
    private File workingDir;
    private Configuration configuration;
    @Setter
    private static Class<? extends Configuration> configClazz = Configuration.class;

    public static ConfigurationHandler getInstance() {
        if (instance == null) instance = new ConfigurationHandler();
        return instance;
    }

    public static Configuration getConfiguration() {
        return getInstance().configuration;
    }

    private void read() {
        log.info("Reading config file '" + configFile.getAbsolutePath() + "'");
        try {
            String json = "{}";
            if (configFile.exists()) {
                json = FileUtils.readFileToString(configFile, Definitions.STD_CHARSET);
                // Migrate from first version:
                if (json.contains("repo-configs")) {
                    json = json.replace("repo-configs", "repoConfigs");
                    json = json.replace("display_name", "displayName");
                }
            }
            this.configuration = JsonUtils.fromJson(configClazz, json);
            if (this.configuration == null) {
                try {
                    this.configuration = configClazz.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    log.error("Internal error: Can't instantiate object of type '" + configClazz + "': " + ex.getMessage(), ex);
                    return;
                }
            }
            if (this.configuration.getRepoConfigs() != null) {
                for (BorgRepoConfig repoConfig : this.configuration.getRepoConfigs()) {
                    if (StringUtils.isBlank(repoConfig.getDisplayName())) {
                        repoConfig.setDisplayName(repoConfig.getRepo());
                    }
                }
            }
            this.configuration.setWorkingDir(workingDir);
        } catch (IOException ex) {
            log.error("Error while trying to read from config file: " + configFile.getAbsolutePath() + ": " + ex.getMessage(), ex);
            return;
        }
    }

    public void save() {
        if (this.configuration.getRepoConfigs() != null) {
            for (BorgRepoConfig repoConfig : this.configuration.getRepoConfigs()) {
                if (StringUtils.isNotBlank(repoConfig.getPasswordCommand())) {
                    log.info("Removing password command from config because password command is given: " + repoConfig.getPasswordCommand());
                    repoConfig.setPassphrase(null); // Don't use password (anymore) if password command is available.
                }
            }
        }
        String json = JsonUtils.toJson(configuration, true);
        try {
            if (configFile.exists()) {
                // Create backup-file first:
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss'-'");
                File configBackupFile = new File(configBackupDir, formatter.format(new Date()) + CONFIG_FILENAME);
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
        configBackupDir = new File(workingDir, CONFIG_BACKUP_DIR);
        if (!configBackupDir.exists()) {
            log.info("Creating borg-butlers backup directory: " + configBackupDir.getAbsolutePath());
            configBackupDir.mkdirs();
        }
        read();
    }
}
