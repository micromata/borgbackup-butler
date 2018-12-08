package de.micromata.borgbutler.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import de.micromata.borgbutler.json.borg.ArchiveList;
import de.micromata.borgbutler.json.borg.RepoInfo;
import de.micromata.borgbutler.json.borg.RepoList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ButlerCache {
    private static Logger log = LoggerFactory.getLogger(ButlerCache.class);
    public static final String CACHE_DIR_NAME = ".borgbutler";
    private static ButlerCache instance = new ButlerCache();

    private EntityCache<RepoInfo> repoInfoCache;
    private EntityCache<RepoList> repoListCache;
    private EntityCache<ArchiveList> archiveListCache;
    private List<EntityCache> caches;

    @JsonIgnore
    private File cacheDir;

    public static ButlerCache getInstance() {
        return instance;
    }

    public static EntityCache<RepoInfo> getRepoInfoCache() {
        return instance.repoInfoCache;
    }

    public static EntityCache<RepoList> getRepoListCache() {
        return instance.repoListCache;
    }

    public static EntityCache<ArchiveList> getArchiveListCache() {
        return instance.archiveListCache;
    }

    public void read() {
        for (EntityCache cache : caches) {
            cache.read();
        }
    }

    public void save() {
        for (EntityCache cache : caches) {
            cache.save();
        }
    }

    /**
     * Removes all cache files and clears all caches.
     */
    public void removeAllCacheFiles() {
        File[] files = cacheDir.listFiles();
        for (File file : files) {
            if (EntityCache.isCacheFile(file)) {
                log.info("Deleting cache file: " + file.getAbsolutePath());
                file.delete();
            }
        }
        for (EntityCache cache : caches) {
            cache.clear();
        }
    }

    private ButlerCache() {
        String homeDir = System.getProperty("user.home");
        cacheDir = new File(homeDir, CACHE_DIR_NAME);
        if (!cacheDir.exists()) {
            log.info("Creating cache dir: " + cacheDir.getAbsolutePath());
            cacheDir.mkdir();
        }
        caches = new ArrayList<>();
        caches.add(repoInfoCache = new EntityCache<RepoInfo>(cacheDir, EntityCache.CACHE_REPO_INFOS_BASENAME, new TypeReference<List<RepoInfo>>() {
        }));
        caches.add(repoListCache = new EntityCache<>(cacheDir, EntityCache.CACHE_REPO_LISTS_BASENAME, new TypeReference<List<RepoList>>() {
        }));
        caches.add(archiveListCache = new EntityCache<>(cacheDir, EntityCache.CACHE_ARCHIVE_LISTS_BASENAME, new TypeReference<List<ArchiveList>>() {
        }));
    }
}
