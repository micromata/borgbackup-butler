package de.micromata.borgbutler.server;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class BorgVersion {
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private boolean versionOK = false;
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private String version;
}
