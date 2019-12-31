package de.micromata.borgbutler.json.borg;

import java.io.Serializable;
import java.util.List;

/**
 * Result of <tt>borg info repo::archive</tt>.
 */
public class BorgArchiveInfo implements Serializable {
    private static final long serialVersionUID = -4200553322856662346L;
    private List<BorgArchive2> archives;
    private BorgCache cache;
    private BorgEncryption encryption;
    private BorgRepository repository;

    public List<BorgArchive2> getArchives() {
        return this.archives;
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
