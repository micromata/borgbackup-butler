package de.micromata.borgbutler.json.borg;

import lombok.Getter;

import java.io.Serializable;

public class BorgEncryption implements Serializable {
    private static final long serialVersionUID = -4867140003118289187L;
    @Getter
    private String mode;
}
