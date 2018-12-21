package de.micromata.borgbutler.config;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class ConfigHandlerTest {
    @Test
    void readWriteTest() throws IOException {
        File workingDir = ConfigurationHandler.getInstance().getWorkingDir();
        File origConfigFile = new File(workingDir, ".borgbutler-orig.json");
        FileUtils.copyFile(ConfigurationHandler.getInstance().getConfigFile(), origConfigFile);
        ConfigurationHandler.getInstance().save();
        FileUtils.copyFile(origConfigFile, ConfigurationHandler.getInstance().getConfigFile());
    }
}
