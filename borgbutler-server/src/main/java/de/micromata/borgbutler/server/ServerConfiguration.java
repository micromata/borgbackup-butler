package de.micromata.borgbutler.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.micromata.borgbutler.config.Configuration;
import de.micromata.borgbutler.config.ConfigurationHandler;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Transient;

public class ServerConfiguration extends Configuration {
    private static Logger log = LoggerFactory.getLogger(ServerConfiguration.class);
    private final static String[] SUPPORTED_LANGUAGES = {"en", "de"};
    public static final int WEBSERVER_PORT_DEFAULT = 9042;
    private static final boolean SHOW_TEST_DATA_PREF_DEFAULT = false;
    private static final boolean WEB_DEVELOPMENT_MODE_PREF_DEFAULT = false;

    private static String applicationHome;

    private int port = WEBSERVER_PORT_DEFAULT;
    @Getter
    @Setter
    @JsonIgnore
    private boolean showTestData = SHOW_TEST_DATA_PREF_DEFAULT;
    private boolean webDevelopmentMode = WEB_DEVELOPMENT_MODE_PREF_DEFAULT;

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
        this.showTestData = other.showTestData;
        this.webDevelopmentMode = other.webDevelopmentMode;
    }
}
