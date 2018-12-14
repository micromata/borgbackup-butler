package de.micromata.borgbutler.json.borg;

import lombok.Getter;

import java.io.Serializable;
import java.util.List;

/**
 * Result of borg list repo
 */
public class BorgRepoList implements Serializable {
    private static final long serialVersionUID = 1006757749929526034L;
    @Getter
    private List<BorgArchive> archives;
    @Getter
    private BorgEncryption encryption;
    @Getter
    private BorgRepository repository;
}
