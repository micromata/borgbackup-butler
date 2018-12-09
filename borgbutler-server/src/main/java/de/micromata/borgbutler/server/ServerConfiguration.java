package de.micromata.borgbutler.server;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Transient;

public class ServerConfiguration {
    private static Logger log = LoggerFactory.getLogger(ServerConfiguration.class);
    private final static String[] SUPPORTED_LANGUAGES = {"en", "de"};
    private static String applicationHome;

    private int port;
    private boolean webDevelopmentMode = false;
    private boolean templatesDirModified = false;

    public static ServerConfiguration getDefault() {
        return ServerConfigurationHandler.getDefaultConfiguration();
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

    public void resetModifiedFlag() {
        templatesDirModified = false;
    }

    @Transient
    public boolean isTemplatesDirModified() {
        return templatesDirModified;
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
        this.port = other.port;
        this.webDevelopmentMode = other.webDevelopmentMode;
    }
}
