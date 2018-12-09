package de.micromata.borgbutler.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.micromata.borgbutler.config.ConfigurationHandler;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ButlerCache {
    private static Logger log = LoggerFactory.getLogger(ButlerCache.class);
    public static final String CACHE_DIR_NAME = "caches";
    private static ButlerCache instance = new ButlerCache();

    @Getter
    private RepoInfoCache repoInfoCache;
    @Getter
    private RepoListCache repoListCache;
    private ArchiveListCache archiveListCache;
    private List<AbstractElementsCache> caches;

    @JsonIgnore
    private File cacheDir;

    public static ButlerCache getInstance() {
        return instance;
    }

    public void read() {
        for (AbstractElementsCache cache : caches) {
            cache.read();
        }
    }

    public void save() {
        for (AbstractElementsCache cache : caches) {
            cache.save();
        }
    }

    /**
     * Removes all cache files and clears all caches.
     */
    public void removeAllCacheFiles() {
        File[] files = cacheDir.listFiles();
        for (File file : files) {
            if (AbstractElementsCache.isCacheFile(file)) {
                log.info("Deleting cache file: " + file.getAbsolutePath());
                file.delete();
            }
        }
        for (AbstractElementsCache cache : caches) {
            cache.clear();
        }
    }

    private ButlerCache() {
        cacheDir = new File(ConfigurationHandler.getInstance().getWorkingDir(), CACHE_DIR_NAME);
        if (!cacheDir.exists()) {
            log.info("Creating cache dir: " + cacheDir.getAbsolutePath());
            cacheDir.mkdir();
        }
        caches = new ArrayList<>();
        caches.add(repoInfoCache = new RepoInfoCache(cacheDir));
        caches.add(repoListCache = new RepoListCache(cacheDir));
        caches.add(archiveListCache = new ArchiveListCache(cacheDir));
    }
}
