package de.micromata.borgbutler.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.TimeZone;

public class RunningMode {
    private static Logger log = LoggerFactory.getLogger(RunningMode.class);
    private static OSType osType;

    public enum Mode {PRODUCTION, DEVELOPMENT}

    public enum ServerType {DESKTOP, SERVER}

    public enum UserManagement {SINGLE}

    public enum OSType {MAC_OS, WINDOWS, LINUX, FREEBSD, OTHER}

    private static boolean running;
    private static File baseDir;
    private static Boolean development;
    private static ServerType serverType;
    private static UserManagement userManagement = UserManagement.SINGLE;

    public static Mode getMode() {
        return isDevelopmentMode() ? Mode.DEVELOPMENT : Mode.PRODUCTION;
    }

    public static boolean isDevelopmentMode() {
        if (development == null) {
            development = new File(ServerConfiguration.getApplicationHome(), "borgbutler-core").exists();
            if (development) {
                log.warn("*** Starting BorgButler server in development mode. This mode shouldn't be used in production environments. ***");
            }
        }
        return development;
    }

    public static OSType getOSType() {
        if (osType == null) {
            String osTypeString = System.getProperty("os.name");
            if (osTypeString == null) {
                osType = OSType.OTHER;
            } else if (osTypeString.toLowerCase().contains("mac")) {
                osType = OSType.MAC_OS;
            } else if (osTypeString.toLowerCase().contains("win")) {
                osType = OSType.WINDOWS;
            } else if (osTypeString.toLowerCase().contains("linux")) {
                osType = OSType.LINUX;
            } else if (osTypeString.toLowerCase().contains("freebsd")) {
                osType = OSType.FREEBSD;
            } else {
                osType = OSType.OTHER;
            }
        }
        return osType;
    }

    public static ServerType getServerType() {
        return serverType;
    }

    public static void setServerType(ServerType serverType) {
        if (RunningMode.serverType != null && serverType != RunningMode.serverType) {
            throw new IllegalArgumentException("Can't set server-type twice with different values: new='"
                    + serverType + "', old='" + RunningMode.serverType + "'.");
        }
        RunningMode.serverType = serverType;
    }

    public static UserManagement getUserManagement() {
        return userManagement;
    }

    /**
     * After setting all values you should call this method for a logging output with all current settings.
     */
    public static void logMode() {
        log.info("Starting " + Version.getInstance().getAppName() + " " + Version.getInstance().getVersion()
                + " (" + Version.getInstance().formatBuildDateISO(TimeZone.getDefault())
                + ") with: mode='" + RunningMode.getMode() + "', serverType='" + RunningMode.serverType
                + "', home dir='" + ServerConfiguration.getApplicationHome() + "', javaVersion='"
                + System.getProperty("java.version") + "'.");
    }
}
