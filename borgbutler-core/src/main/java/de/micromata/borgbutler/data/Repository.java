package de.micromata.borgbutler.data;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.json.borg.BorgCache;
import de.micromata.borgbutler.json.borg.BorgEncryption;
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
    String name;
    /**
     * A name describing this config. Only used for displaying purposes. This is automatically set with the name
     * of the repository configuration.
     *
     * @see BorgRepoConfig#getDisplayName()
     */
    String displayName;
    private String id;
    /**
     * Date given by Borg server.
     */
    private String lastModified;
    /**
     * Last date of getting this object from Borg server.
     */
    private String lastCacheRefresh;
    private String location;

    private String securityDir;
    private BorgCache cache;
    private BorgEncryption encryption;

    /**
     * Might be null.
     */
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

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getId() {
        return this.id;
    }

    public String getLastModified() {
        return this.lastModified;
    }

    public String getLastCacheRefresh() {
        return this.lastCacheRefresh;
    }

    public String getLocation() {
        return this.location;
    }

    public String getSecurityDir() {
        return this.securityDir;
    }

    public BorgCache getCache() {
        return this.cache;
    }

    public BorgEncryption getEncryption() {
        return this.encryption;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public void setLastCacheRefresh(String lastCacheRefresh) {
        this.lastCacheRefresh = lastCacheRefresh;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSecurityDir(String securityDir) {
        this.securityDir = securityDir;
    }

    public void setCache(BorgCache cache) {
        this.cache = cache;
    }

    public void setEncryption(BorgEncryption encryption) {
        this.encryption = encryption;
    }

    public void setArchives(SortedSet<Archive> archives) {
        this.archives = archives;
    }
}
