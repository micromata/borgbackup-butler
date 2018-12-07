package de.micromata.borgbutler.config;

import de.micromata.borgbutler.json.JsonUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class ConfigurationHandler {
    private static Logger log = LoggerFactory.getLogger(ConfigurationHandler.class);
    private static ConfigurationHandler instance = new ConfigurationHandler();
    private static final String CONFIG_FILENAME = ".borgbutler.json";
    private File configFile;
    private Configuration configuration = new Configuration();

    public static ConfigurationHandler getInstance() {
        return instance;
    }

    public static Configuration getConfiguration() {
        return instance.configuration;
    }

    public void read() {
        try {
            String json = FileUtils.readFileToString(configFile, Charset.forName("UTF-8"));
            this.configuration = JsonUtils.fromJson(Configuration.class, json);
        } catch (IOException ex) {
            log.error("Error while trying to read from config file: " + configFile.getAbsolutePath() + ": " + ex.getMessage(), ex);
            return;
        }
    }

    public void write() {
        String json = JsonUtils.toJson(configuration);
        try {
            FileUtils.write(configFile, json, Charset.forName("UTF-8"));
        } catch (IOException ex) {
            log.error("Error while trying to write config file: " + configFile.getAbsolutePath() + ": " + ex.getMessage(), ex);
        }
    }

    private ConfigurationHandler() {
        configFile = new File(System.getProperty("user.home"), CONFIG_FILENAME);
    }
}
