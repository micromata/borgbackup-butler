package de.micromata.borgbutler.data;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.json.borg.BorgCache;
import de.micromata.borgbutler.json.borg.BorgEncryption;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Part of Borg json objects to refer objects to repositories.
 */
public class Repository implements Serializable {
    private static final long serialVersionUID = 1278802519434516280L;
    /**
     * The repo configured for borg.
     *
     * @see BorgRepoConfig#getRepo()
     */
    @Getter
    @Setter
    String name;
    /**
     * A name describing this config. Only used for displaying purposes. This is automatically set with the name
     * of the repository configuration.
     *
     * @see BorgRepoConfig#getDisplayName()
     */
    @Getter
    @Setter
    String displayName;
    @Getter
    @Setter
    private String id;
    /**
     * Date given by Borg server.
     */
    @Getter
    @Setter
    private String lastModified;
    /**
     * Last date of getting this object from Borg server.
     */
    @Getter
    @Setter
    private String lastCacheRefresh;
    @Getter
    @Setter
    private String location;

    @Getter
    @Setter
    private String securityDir;
    @Getter
    @Setter
    private BorgCache cache;
    @Getter
    @Setter
    private BorgEncryption encryption;

    /**
     * Might be null.
     */
    @Getter
    @Setter
    private SortedSet<Archive> archives;

    public Repository add(Archive archive) {
        if (this.archives == null) {
            this.archives = new TreeSet<>();
        }
        this.archives.add(archive);
        return this;
    }

    /**
     * Is <tt>borg list repo</tt> already called?
     * @return
     */
    public boolean isArchivesLoaded() {
        return CollectionUtils.isNotEmpty(archives);
    }
}
