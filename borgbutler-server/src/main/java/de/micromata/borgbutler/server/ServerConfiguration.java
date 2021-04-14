package de.micromata.borgbutler.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.micromata.borgbutler.cache.ButlerCache;
import org.micromata.borgbutler.config.Configuration;
import org.micromata.borgbutler.config.ConfigurationHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerConfiguration extends Configuration {
    private static Logger log = LoggerFactory.getLogger(ServerConfiguration.class);
    private final static String[] SUPPORTED_LANGUAGES = {"en", "de"};
    public static final int WEBSERVER_PORT_DEFAULT = 9042;
    private static final boolean WEB_DEVELOPMENT_MODE_PREF_DEFAULT = false;

    private static String applicationHome;

    private int port = WEBSERVER_PORT_DEFAULT;
    private boolean webDevelopmentMode = WEB_DEVELOPMENT_MODE_PREF_DEFAULT;
    @JsonProperty
    public String getCacheDir() {
        return ButlerCache.getInstance().getCacheDir().getAbsolutePath();
    }

    public static ServerConfiguration get() {
        return (ServerConfiguration)ConfigurationHandler.getConfiguration();
    }

    public static String[] getSupportedLanguages() {
        return SUPPORTED_LANGUAGES;
    }

    public static String getApplicationHome() {
        if (applicationHome == null) {
            applicationHome = System.getProperty("applicationHome");
            if (StringUtils.isBlank(applicationHome)) {
                applicationHome = System.getProperty("user.dir");
                log.info("applicationHome is not given as JVM   parameter. Using current working dir (OK for start in IDE): " + applicationHome);
            }
        }
        return applicationHome;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * If true, CrossOriginFilter will be set.
     */
    public boolean isWebDevelopmentMode() {
        return webDevelopmentMode;
    }

    public void setWebDevelopmentMode(boolean webDevelopmentMode) {
        this.webDevelopmentMode = webDevelopmentMode;
    }

    public void copyFrom(ServerConfiguration other) {
        super.copyFrom(other);
        this.port = other.port;
        this.webDevelopmentMode = other.webDevelopmentMode;
    }
}
