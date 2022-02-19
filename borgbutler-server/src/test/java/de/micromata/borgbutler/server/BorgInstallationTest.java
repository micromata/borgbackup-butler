package de.micromata.borgbutler.server;

import de.micromata.borgbutler.config.ConfigurationHandler;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class BorgInstallationTest {
    private Logger log = LoggerFactory.getLogger(BorgInstallationTest.class);

    @Test
    void foo() throws Exception {
        ConfigurationHandler.getConfiguration().setBorgCommand(null);
        BorgInstallation borgInstallation = BorgInstallation.getInstance();
        borgInstallation.initialize();
        ConfigurationHandler.getConfiguration().setBorgCommand("borg");
        borgInstallation.initialize();
    }


    @Test
    void configureTest() {
        ConfigurationHandler.setConfigClazz(ServerConfiguration.class);
        BorgInstallation borgInstallation = BorgInstallation.getInstance();
        borgInstallation.initialize();
        BorgConfig borgConfig = new BorgConfig();
        borgConfig.setVersion("1.1.15");
        borgConfig.setBorgBinary("freebsd64");
        ServerConfiguration serverConfig = ServerConfiguration.get();
        borgInstallation.configure(serverConfig, borgConfig);
        String expected = "freebsd64-1.1.15";
        assertTrue(serverConfig.getBorgCommand().endsWith(expected), "String '" + serverConfig.getBorgCommand() + "' should end with '" + expected + "'.");
    }

    @Test
    void downloadTest() {
        String version = new BorgConfig().getVersion();
        checkDownload(RunningMode.OSType.LINUX, "borg-linux64-" + version);
        checkDownload(RunningMode.OSType.MAC_OS, "borg-macosx64-" + version);
        checkDownload(RunningMode.OSType.FREEBSD, "borg-freebsd64-" + version);
        assertNull(BorgInstallation.getInstance().download(RunningMode.OSType.WINDOWS));
    }

    private void checkDownload(RunningMode.OSType osType, String expectedName) {
        File file = BorgInstallation.getInstance().download(osType);
        assertEquals(expectedName, file.getName());
        assertTrue(file.canExecute());
    }
}
