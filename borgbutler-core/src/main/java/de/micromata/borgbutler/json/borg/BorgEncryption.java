package de.micromata.borgbutler.json.borg;

import java.io.Serializable;

public class BorgEncryption implements Serializable {
    private static final long serialVersionUID = -4867140003118289187L;
    private String mode;

    public String getMode() {
        return this.mode;
    }
}
