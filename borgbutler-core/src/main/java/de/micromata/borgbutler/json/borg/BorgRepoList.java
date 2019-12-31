package de.micromata.borgbutler.json.borg;

import java.io.Serializable;
import java.util.List;

/**
 * Result of borg list repo
 */
public class BorgRepoList implements Serializable {
    private static final long serialVersionUID = 1006757749929526034L;
    private List<BorgArchive> archives;
    private BorgEncryption encryption;
    private BorgRepository repository;

    public List<BorgArchive> getArchives() {
        return this.archives;
    }

    public BorgEncryption getEncryption() {
        return this.encryption;
    }

    public BorgRepository getRepository() {
        return this.repository;
    }
}
