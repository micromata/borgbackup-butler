package de.micromata.borgbutler.server;

public class BorgVersion {
    private String binariesDownloadVersion = "1.1.8";
    private String binariesDownloadUrl = "https://github.com/borgbackup/borg/releases/download/" + binariesDownloadVersion + "/";
    private String[][] borgBinaries = {
            {"freebsd64", "FreeBSD 64"},
            {"linux32", "Linux 32"},
            {"linux64", "Linux 64"},
            {"macosx64", "MacOS X 64"}};

    private String minimumRequiredBorgVersion = "1.1.8";

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

    public String getBinariesDownloadUrl() {
        return this.binariesDownloadUrl;
    }

    public String[][] getBorgBinaries() {
        return this.borgBinaries;
    }

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
}
