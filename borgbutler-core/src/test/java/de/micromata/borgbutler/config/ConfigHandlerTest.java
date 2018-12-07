package de.micromata.borgbutler.config;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class ConfigHandlerTest {
    @Test
    void readWriteTest() throws IOException {
        File origConfigFile = new File(System.getProperty("user.home"), ".borgbutler-orig.json");
        FileUtils.copyFile(ConfigurationHandler.getInstance().getConfigFile(), origConfigFile);
        Configuration configuration = ConfigurationHandler.getConfiguration();
        ConfigurationHandler.getInstance().read();
        ConfigurationHandler.getInstance().write();
        FileUtils.copyFile(origConfigFile, ConfigurationHandler.getInstance().getConfigFile());
    }
}
