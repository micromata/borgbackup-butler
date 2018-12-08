package de.micromata.borgbutler.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ButlerCache {
    private static Logger log = LoggerFactory.getLogger(ButlerCache.class);
    public static final String CACHE_DIR_NAME = ".borgbutler";
    private static ButlerCache instance = new ButlerCache();

    private RepoInfoCache repoInfoCache;
    private RepoListCache repoListCache;
    private List<AbstractCache> caches;

    @JsonIgnore
    private File cacheDir;

    public static ButlerCache getInstance() {
        return instance;
    }

    public static RepoInfoCache getRepoInfoCache() {
        return instance.repoInfoCache;
    }

    public static RepoListCache getRepoListCache() {
        return instance.repoListCache;
    }

    public void read() {
        for (AbstractCache cache : caches) {
            cache.read();
        }
    }

    public void save() {
        for (AbstractCache cache : caches) {
            cache.save();
        }
    }

    /**
     * Removes all cache files and clears all caches.
     */
    public void removeAllCacheFiles() {
        File[] files = cacheDir.listFiles();
        for (File file : files) {
            if (AbstractCache.isCacheFile(file)) {
                log.info("Deleting cache file: " + file.getAbsolutePath());
                file.delete();
            }
        }
        for (AbstractCache cache : caches) {
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
        repoInfoCache = new RepoInfoCache(cacheDir);
        repoListCache = new RepoListCache(cacheDir);
        caches = new ArrayList<>();
        caches.add(repoInfoCache);
        caches.add(repoListCache);
    }
}
