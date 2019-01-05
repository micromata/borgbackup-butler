package de.micromata.borgbutler.data;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.json.borg.BorgCache;
import de.micromata.borgbutler.json.borg.BorgEncryption;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Part of Borg json objects to refer objects to repositories.
 */
public class Repository implements Serializable {
    private static Logger log = LoggerFactory.getLogger(Repository.class);
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
    @Setter
    private SortedSet<Archive> archives;

    public Repository add(Archive archive) {
        synchronized (this) {
            if (this.archives == null) {
                this.archives = new TreeSet<>();
            }
        }
        synchronized (this.archives) {
            this.archives.add(archive);
        }
        return this;
    }

    public Archive getArchive(String idOrName) {
        if (archives == null) {
            log.warn("Can't get archive '" + idOrName + "' from repository '" + name + "'. Archives not yet loaded or don't exist.");
            return null;
        }
        synchronized (this.archives) {
            for (Archive archive : archives) {
                if (StringUtils.equals(idOrName, archive.getId()) || StringUtils.equals(idOrName, archive.getName())) {
                    return archive;
                }
            }
        }
        log.warn("Archive '" + idOrName + "' not found in repository '" + name + "'.");
        return null;
    }

    /**
     * Is <tt>borg list repo</tt> already called?
     *
     * @return
     */
    public boolean isArchivesLoaded() {
        return CollectionUtils.isNotEmpty(archives);
    }

    public Collection<Archive> getArchives() {
        if (this.archives == null) {
            return null;
        }
        synchronized (this.archives) {
            return Collections.unmodifiableSet(this.archives);
        }
    }
}
