package de.micromata.borgbutler.server;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class BorgVersion {
    @Getter
    private String binariesDownloadVersion = "1.1.8";
    @Getter
    private String binariesDownloadUrl = "https://github.com/borgbackup/borg/releases/download/" + binariesDownloadVersion + "/";
    @Getter
    private String[][] borgBinaries = {
            {"freebsd64", "FreeBSD 64"},
            {"linux32", "Linux 32"},
            {"linux64", "Linux 64"},
            {"macosx64", "MacOS X 64"}};

    @Getter
    private String minimumRequiredBorgVersion = "1.1.8";

    /**
     * One of the values "macosx64", "linux64" etc. for using a binary provided by BorgButler or null / "manual" for
     * using a manual installed borg version.
     */
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private String borgBinary;
    /**
     * The path of the borg command to use.
     */
    @Getter
    @Setter
    private String borgCommand;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private boolean versionOK = false;
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private String version;
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private String statusMessage;

    public BorgVersion copyFrom(BorgVersion other) {
        this.borgCommand = other.borgCommand;
        this.borgBinary = other.borgBinary;
        this.versionOK = other.versionOK;
        this.version = other.version;
        this.statusMessage = other.statusMessage;
        return this;
    }
}
