package de.micromata.borgbutler.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ButlerCache {
    private static Logger log = LoggerFactory.getLogger(ButlerCache.class);
    public static final String CACHE_DIR_NAME = ".borgbutler";
    private static ButlerCache instance = new ButlerCache();

    private RepoInfoCache reposCache;

    @JsonIgnore
    private File cacheDir;

    public static ButlerCache getInstance() {
        return instance;
    }

    public static RepoInfoCache getReposCache() {
        return instance.reposCache;
    }

    public void save() {
        reposCache.save();
    }

    private ButlerCache() {
        String homeDir = System.getProperty("user.home");
        cacheDir = new File(homeDir, CACHE_DIR_NAME);
        if (!cacheDir.exists()) {
            log.info("Creating cache dir: " + cacheDir.getAbsolutePath());
            cacheDir.mkdir();
        }
        reposCache = new RepoInfoCache(cacheDir);
    }
}
