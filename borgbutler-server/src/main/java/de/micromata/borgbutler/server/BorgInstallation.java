package de.micromata.borgbutler.server;

import de.micromata.borgbutler.BorgCommands;
import de.micromata.borgbutler.config.Configuration;
import de.micromata.borgbutler.config.ConfigurationHandler;
import org.apache.commons.io.FileUtils;
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
        String version = BorgCommands.version();
        if (version != null) {
            log.info("Using borg '" + configuration.getBorgCommand() + "', version: " + version);
            return;
        }
        String[] binary = getBinary(RunningMode.getOSType());
        download(binary);
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
        if (os == null) {
            return null;
        }
        for (String[] binary : ConfigurationHandler.getConfiguration().getBorgBinaries()) {
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
        String url = ConfigurationHandler.getConfiguration().getBinariesDownloadUrl() + "borg-" + binary[0];
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
            File file = new File(getBinaryDir(), "borg-" + binary[0]);
            FileUtils.copyInputStreamToFile(response.getEntity().getContent(), file);
            log.info("Downloaded: " + file.getAbsolutePath());
            file.setExecutable(true, false);
            return file;
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    private File getBinaryDir() {
        File dir = new File(ConfigurationHandler.getInstance().getWorkingDir(), "bin");
        if (!dir.exists()) {
            log.info("Creating binary directory: " + dir.getAbsolutePath());
            dir.mkdirs();
        }
        return dir;
    }

    private BorgInstallation() {
    }
}
