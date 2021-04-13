package de.micromata.borgbutler.server;

import org.apache.commons.lang3.StringUtils;

public class BorgVersion {
    public static final String BORG_DEFAULT_DOWNLOAD_VERSION = "1.1.16";

    private static final String BORG_VERSION = BORG_DEFAULT_DOWNLOAD_VERSION;

    private String binariesDownloadVersion = BORG_DEFAULT_DOWNLOAD_VERSION;

    private String[][] borgBinaries = {
            {"freebsd64", "FreeBSD 64"},
            {"linux32", "Linux 32"},
            {"linux64", "Linux 64"},
            {"macosx64", "MacOS X 64"}};

    private String minimumRequiredBorgVersion = "1.1.8";

    public String getBinariesDownloadUrl() {
        return "https://github.com/borgbackup/borg/releases/download/" + binariesDownloadVersion + "/";
    }

    /**
     * One of the values "macosx64", "linux64" etc. for using a binary provided by BorgButler or null / "manual" for
     * using a manual installed borg version.
     */
    private String borgBinary;

    private boolean versionOK = false;
    private String version;
    private String statusMessage;

    public BorgVersion copyFrom(BorgVersion other) {
        this.borgBinary = other.borgBinary;
        this.versionOK = other.versionOK;
        this.version = other.version;
        this.statusMessage = other.statusMessage;
        return this;
    }

    public String getBinariesDownloadVersion() {
        return this.binariesDownloadVersion;
    }

    public void setBinariesDownloadVersion(String binariesDownloadVersion) {
        if (StringUtils.isNotBlank(binariesDownloadVersion)) {
            this.binariesDownloadVersion = binariesDownloadVersion;
        } else {
            this.binariesDownloadVersion = BORG_DEFAULT_DOWNLOAD_VERSION;
        }
    }

    public String[][] getBorgBinaries() {
        return this.borgBinaries;
    }

    /**
     * @return The minimal required borg version (installed on host).
     */
    public String getMinimumRequiredBorgVersion() {
        return this.minimumRequiredBorgVersion;
    }

    public String getBorgBinary() {
        return this.borgBinary;
    }

    public boolean isVersionOK() {
        return this.versionOK;
    }

    public String getVersion() {
        return this.version;
    }

    public String getStatusMessage() {
        return this.statusMessage;
    }

    BorgVersion setBorgBinary(String borgBinary) {
        this.borgBinary = borgBinary;
        return this;
    }

    BorgVersion setVersionOK(boolean versionOK) {
        this.versionOK = versionOK;
        return this;
    }

    BorgVersion setVersion(String version) {
        this.version = version;
        return this;
    }

    BorgVersion setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        return this;
    }

    public static int compareVersions(String thisVersion, String otherVersion) {
        String[] thisParts = checkVersion(thisVersion);
        String[] otherParts = checkVersion(otherVersion);
        int length = Math.max(thisParts.length, otherParts.length);
        for (int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ?
                    Integer.parseInt(thisParts[i]) : 0;
            int thatPart = i < otherParts.length ?
                    Integer.parseInt(otherParts[i]) : 0;
            if (thisPart < thatPart)
                return -1;
            if (thisPart > thatPart)
                return 1;
        }
        return 0;
    }

    // https://stackoverflow.com/questions/198431/how-do-you-compare-two-version-strings-in-java
    public static String[] checkVersion(String version) {
        if (version == null) {
            throw new IllegalArgumentException("Version can not be null");
        }
        if (!version.matches("[0-9]+(\\.[0-9]+)*")) {
            throw new IllegalArgumentException("Invalid version format: " + version);
        }
        return version.split("\\.");
    }
}
