package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Result of borg list repo
 */
public class RepoList implements Serializable {
    private static final long serialVersionUID = 1006757749929526034L;
    @Getter
    private List<Archive> archives;
    @Getter
    private Encryption encryption;
    @Getter
    private Repository repository;
    @Getter
    @Setter
    @JsonIgnore
    protected String originalJson;
}
