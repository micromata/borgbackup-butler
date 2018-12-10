package de.micromata.borgbutler.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.micromata.borgbutler.BorgCommands;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.json.borg.Archive;
import de.micromata.borgbutler.json.borg.FilesystemItem;
import de.micromata.borgbutler.json.borg.RepoInfo;
import de.micromata.borgbutler.json.borg.Repository;
import lombok.Getter;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ButlerCache {
    private static Logger log = LoggerFactory.getLogger(ButlerCache.class);
    public static final String CACHE_DIR_NAME = "caches";
    private static ButlerCache instance = new ButlerCache();

    private JCSCache jcsCache = JCSCache.getInstance();
    private CacheAccess<String, RepoInfo> repoInfoCacheAccess;
    @Getter
    private RepoListCache repoListCache;
    private ArchiveListCache archiveListCache;
    private List<AbstractElementsCache> caches;
    private List<ArchiveFileListCache> archiveFileListCaches;

    @JsonIgnore
    private File cacheDir;

    public static ButlerCache getInstance() {
        return instance;
    }

    public void save() {
        for (AbstractElementsCache cache : caches) {
            cache.save();
        }
    }

    public Repository getRepository(String idOrName) {
        RepoInfo repoInfo = getRepoInfo(idOrName);
        if (repoInfo == null) {
            return null;
        }
        return repoInfo.getRepository();
    }

    public RepoInfo getRepoInfo(String idOrName) {
        BorgRepoConfig repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(idOrName);
        RepoInfo repoInfo = repoInfoCacheAccess.get(repoConfig.getRepo());
        if (repoInfo == null) {
            repoInfo = BorgCommands.info(repoConfig);
            repoInfoCacheAccess.put(repoConfig.getRepo(), repoInfo);
        }
        if (repoInfo == null) {
            log.warn("Repo with name '" + idOrName + "' not found.");
        }
        return repoInfo;
    }

    public List<Repository> getAllRepositories() {
        List<Repository> repositories = new ArrayList<>();
        for (BorgRepoConfig repoConfig : ConfigurationHandler.getConfiguration().getRepoConfigs()) {
            RepoInfo repoInfo = getRepoInfo(repoConfig.getName());
            if (repoInfo == null) {
                continue;
            }
            repositories.add(repoInfo.getRepository());
        }
        return repositories;
    }

    public List<FilesystemItem> getArchiveContent(BorgRepoConfig repoConfig, Archive archive) {
        if (archive == null || StringUtils.isBlank(archive.getArchive())) {
            return null;
        }
        ArchiveFileListCache cache = null;
        for (ArchiveFileListCache existingCache : archiveFileListCaches) {
            if (archive.equals(existingCache.getArchive())) {
                // Cache is already known:
                cache = existingCache;
                break;
            }
        }
        if (cache == null) {
            cache = new ArchiveFileListCache(cacheDir, repoConfig, archive);
        }
        return cache.getContent(repoConfig);
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
        caches.add(repoListCache = new RepoListCache(cacheDir));
        caches.add(archiveListCache = new ArchiveListCache(cacheDir));
        archiveFileListCaches = new ArrayList<>();
        this.repoInfoCacheAccess = jcsCache.getJCSCache();
    }
}
