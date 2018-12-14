package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;

/**
 * Result of borg info repo
 */
public class BorgRepoInfo implements Serializable {
    private static final long serialVersionUID = -1588038325129799400L;
    @Getter
    @JsonProperty("security_dir")
    private String securityDir;
    @Getter
    private BorgCache cache;
    @Getter
    private BorgEncryption encryption;
    @Getter
    private BorgRepository repository;
}
