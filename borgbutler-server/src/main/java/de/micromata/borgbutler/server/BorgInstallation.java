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

    public void initialize() {
        Configuration configuration = ConfigurationHandler.getConfiguration();
        if (StringUtils.isNotBlank(configuration.getBorgCommand())) {
            if (version(configuration)) {
                return;
            }
        }
        initialize(getBinary(RunningMode.getOSType()));
        if (version(configuration)) {
            return;
        }
        BorgVersion borgVersion = ServerConfiguration.get()._getBorgVersion();
        log.warn("No working borg version found. Please configure a borg version with minimal version '" + borgVersion.getMinimumRequiredBorgVersion() + "'.");
    }

    /**
     * Configures a new borg configuration if modifications was done.
     * @param oldVersion The old version before this current modification.
     */
    public void configure(BorgVersion oldVersion) {
        Configuration configuration = ConfigurationHandler.getConfiguration();
        BorgVersion newVersion = ServerConfiguration.get()._getBorgVersion();
        boolean borgBinaryChanged = !StringUtils.equals(oldVersion.getBorgBinary(), newVersion.getBorgBinary());
        boolean manualBorgCommand = "manual".equals(newVersion.getBorgBinary());
        if (manualBorgCommand) {
            boolean borgCommandChanged = !StringUtils.equals(oldVersion.getBorgCommand(), newVersion.getBorgCommand());
            if (borgCommandChanged) {
                configuration.setBorgCommand(newVersion.getBorgCommand());
                version(configuration);
            } else {
                newVersion.copyFrom(oldVersion); // Restore all old settings.
            }
        } else {
            if (borgBinaryChanged) {
                newVersion.setBorgCommand(oldVersion.getBorgCommand()); // Don't modify borg command, if not manual.
                initialize(getBinary(newVersion.getBorgBinary()));
            } else {
                newVersion.copyFrom(oldVersion); // Restore all old settings.
            }
        }
    }

    private boolean initialize(String[] binary) {
        if (binary == null) {
            return false;
        }
        Configuration configuration = ConfigurationHandler.getConfiguration();
        File file = download(binary);
        BorgVersion borgVersion = ServerConfiguration.get()._getBorgVersion();
        if (file != null) {
            configuration.setBorgCommand(file.getAbsolutePath());
            borgVersion.setBorgCommand(file.getAbsolutePath());
            borgVersion.setBorgBinary(binary[0]);
        }
        return version(configuration);
    }

    private boolean version(Configuration configuration) {
        String versionString = BorgCommands.version();
        boolean versionOK = false;
        BorgVersion borgVersion = ServerConfiguration.get()._getBorgVersion();
        String msg = null;
        if (versionString != null) {
            borgVersion.setVersion(versionString);
            int cmp = versionString.compareTo(borgVersion.getMinimumRequiredBorgVersion());
            if (cmp < 0) {
                msg = "Found borg version '" + versionString + "' is less than minimum required version '" + borgVersion.getMinimumRequiredBorgVersion() + "'.";
                log.info(msg);
            } else {
                versionOK = true;
                msg = "Found borg '" + configuration.getBorgCommand() + "', version: " + versionString + " (equals to or newer than '" + borgVersion.getMinimumRequiredBorgVersion()
                        + "', OK).";
                log.info(msg);
            }
        } else {
            msg = "Couldn't execute borg command '" + configuration.getBorgCommand() + "'.";
        }
        borgVersion.setVersionOK(versionOK);
        borgVersion.setStatusMessage(msg);
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
        BorgVersion borgVersion = ServerConfiguration.get()._getBorgVersion();
        for (String[] binary : borgVersion.getBorgBinaries()) {
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
        BorgVersion borgVersion = ServerConfiguration.get()._getBorgVersion();
        String url = borgVersion.getBinariesDownloadUrl() + getDownloadFilename(binary);
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
        BorgVersion borgVersion = ServerConfiguration.get()._getBorgVersion();
        return new File(dir, getDownloadFilename(binary) + "-" + borgVersion.getBinariesDownloadVersion());
    }

    private String getDownloadFilename(String[] binary) {
        return "borg-" + binary[0];
    }

    private BorgInstallation() {
    }
}
