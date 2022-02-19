package de.micromata.borgbutler.server;

import de.micromata.borgbutler.BorgCommands;
import de.micromata.borgbutler.config.Configuration;
import de.micromata.borgbutler.config.ConfigurationHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class BorgInstallation {
    private Logger log = LoggerFactory.getLogger(BorgInstallation.class);
    private static final BorgInstallation instance = new BorgInstallation();

    public static BorgInstallation getInstance() {
        return instance;
    }

    private BorgConfig borgConfig = new BorgConfig();

    public void initialize() {
        Configuration configuration = ConfigurationHandler.getConfiguration();
        if (StringUtils.isNotBlank(configuration.getBorgCommand())) {
            if (version(configuration)) {
                return;
            }
        }
        borgConfig.setVersion(configuration.getBorgVersion());
        initialize(getBinary(RunningMode.getOSType()));
        if (!borgConfig.isVersionOK()) {
            log.warn("No working borg version found. Please configure a borg version with minimal version '" + borgConfig.getMinimumRequiredBorgVersion() + "'.");
        }
    }

    /**
     * Configures a new borg configuration if modifications was done.
     *
     * @param newConfiguration The new configuration with the (new) borg command to use (executable).
     * @param newBorgConfig     The new config, including the id of the borg binary (Mac OS X, Linux 64, manual etc.) and version.
     */
    public void configure(ServerConfiguration newConfiguration, BorgConfig newBorgConfig) {
        ServerConfiguration configuration = ServerConfiguration.get();
        String oldBorgBinary = borgConfig.getBorgBinary();
        String oldVersion = borgConfig.getVersion();
        String newBorgBinary = newBorgConfig.getBorgBinary();
        String newVersion = newConfiguration.getBorgVersion();
        boolean borgBinaryChanged = !StringUtils.equals(oldBorgBinary, newBorgBinary) ||
                !StringUtils.equals(oldVersion, newVersion);
        borgConfig.setBorgBinary(newBorgBinary); // Update borg binary (if changed).
        borgConfig.setVersion(newVersion);
        boolean manualBorgCommand = "manual".equals( newBorgConfig.getBorgBinary());
        if (manualBorgCommand) {
            boolean borgCommandChanged = !StringUtils.equals(newConfiguration.getBorgCommand(), configuration.getBorgCommand());
            if (borgCommandChanged) {
                configuration.setBorgCommand(newConfiguration.getBorgCommand());
                version(configuration);
            }
        } else {
            if (borgBinaryChanged) {
                initialize(getBinary(newBorgBinary));
            }
            newConfiguration.setBorgCommand(configuration.getBorgCommand()); // borg command of this class overwrites new configuration for mode != 'manual'.
            newConfiguration.setBorgVersion(newVersion);
            if (!StringUtils.equals(oldVersion, newVersion)) {
                log.info("Version '" + oldVersion + "' -> '" + newVersion + "'.");
            }
            if (!StringUtils.equals(oldBorgBinary, newBorgBinary)) {
                log.info("Binary '" + oldBorgBinary + "' -> '" + newBorgBinary + "'.");
            }
        }
    }

    private boolean initialize(String[] binary) {
        if (binary == null) {
            return false;
        }
        Configuration configuration = ConfigurationHandler.getConfiguration();
        File file = download(binary);
        if (file != null) {
            configuration.setBorgCommand(file.getAbsolutePath());
            borgConfig.setBorgBinary(binary[0]);
        }
        return version(configuration);
    }

    private boolean version(Configuration configuration) {
        String borgCommand = configuration.getBorgCommand();
        if (StringUtils.isNotBlank(borgCommand)) {
            for (String[] borgBinary : borgConfig.getBorgBinaries()) {
                if (borgCommand.contains(borgBinary[0])) {
                    borgConfig.setBorgBinary(borgBinary[0]);
                    break;
                }
            }
        }
        String versionString = BorgCommands.version();
        boolean versionOK = false;
        String msg = null;
        if (versionString != null) {
            borgConfig.setVersion(versionString);
            int cmp = BorgConfig.compareVersions(versionString, borgConfig.getMinimumRequiredBorgVersion());
            if (cmp < 0) {
                msg = "Found borg version '" + versionString + "' is less than minimum required version '" + borgConfig.getMinimumRequiredBorgVersion() + "'.";
                log.info(msg);
            } else {
                versionOK = true;
                msg = "Found borg '" + configuration.getBorgCommand() + "', version: " + versionString + " (equals to or newer than '" + borgConfig.getMinimumRequiredBorgVersion()
                        + "', OK).";
                log.info(msg);
            }
        } else {
            msg = "Couldn't execute borg command '" + configuration.getBorgCommand() + "'.";
        }
        borgConfig.setVersionOK(versionOK);
        borgConfig.setStatusMessage(msg);
        return versionOK;
    }

    private String[] getBinary(RunningMode.OSType osType) {
        String os = null;
        switch (osType) {
            case MAC_OS:
                os = "mac";
                break;
            case LINUX:
                os = "linux64";
                break;
            case FREEBSD:
                os = "freebsd64";
                break;
        }
        return getBinary(os);
    }

    private String[] getBinary(String os) {
        if (os == null) {
            return null;
        }
        for (String[] binary : borgConfig.getBorgBinaries()) {
            if (binary[0].contains(os)) {
                return binary;
            }
        }
        return null;
    }

    File download(RunningMode.OSType osType) {
        String[] binary = getBinary(osType);
        if (binary == null) {
            log.info("Can't download binary (no binary found for OS '" + osType + "'.");
            return null;
        }
        return download(binary);
    }

    private File download(String[] binary) {
        File file = getBinaryFile(binary);
        if (file.exists()) {
            // File already downloaded, nothing to do.
            return file;
        }
        String url = borgConfig.getBinariesDownloadUrl() + getDownloadFilename(binary);
        log.info("Trying to download borg binary '" + binary[0] + "' (" + binary[1] + ") from url: " + url + "...");
        HttpClientBuilder builder = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build());
        try (CloseableHttpClient httpClient = builder.build()) {
            HttpGet getRequest = new HttpGet(url);

            HttpResponse response = httpClient.execute(getRequest);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }
            FileUtils.copyInputStreamToFile(response.getEntity().getContent(), file);
            log.info("Downloaded: " + file.getAbsolutePath());
            file.setExecutable(true, false);
            return file;
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    private File getBinaryFile(String[] binary) {
        File dir = new File(ConfigurationHandler.getInstance().getWorkingDir(), "bin");
        if (!dir.exists()) {
            log.info("Creating binary directory: " + dir.getAbsolutePath());
            dir.mkdirs();
        }
        return new File(dir, getDownloadFilename(binary) + "-" + borgConfig.getVersion());
    }

    private String getDownloadFilename(String[] binary) {
        return "borg-" + binary[0];
    }

    private BorgInstallation() {
    }

    public BorgConfig getBorgConfig() {
        return this.borgConfig;
    }
}
