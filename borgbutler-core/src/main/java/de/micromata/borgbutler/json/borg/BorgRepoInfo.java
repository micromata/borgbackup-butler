package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Result of borg info repo
 */
public class BorgRepoInfo implements Serializable {
    private static final long serialVersionUID = -1588038325129799400L;
    @JsonProperty("security_dir")
    private String securityDir;
    private BorgCache cache;
    private BorgEncryption encryption;
    private BorgRepository repository;

    public String getSecurityDir() {
        return this.securityDir;
    }

    public BorgCache getCache() {
        return this.cache;
    }

    public BorgEncryption getEncryption() {
        return this.encryption;
    }

    public BorgRepository getRepository() {
        return this.repository;
    }
}
