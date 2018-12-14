package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Result of borg info repo
 */
public class RepoInfo implements Serializable {
    private static final long serialVersionUID = -1588038325129799400L;
    @Getter
    @JsonProperty("security_dir")
    private String securityDir;
    @Getter
    private Cache cache;
    @Getter
    private Encryption encryption;
    @Getter
    private Repository repository;
    @Getter
    @Setter
    @JsonIgnore
    protected String originalJson;
}
