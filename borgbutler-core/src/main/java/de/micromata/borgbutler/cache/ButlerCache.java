package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.BorgCommands;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.Configuration;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.json.borg.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ButlerCache {
    private static Logger log = LoggerFactory.getLogger(ButlerCache.class);
    public static final String CACHE_DIR_NAME = "cache";
    private static ButlerCache instance = new ButlerCache();

    private JCSCache jcsCache;
    private CacheAccess<String, RepoInfo> repoInfoCacheAccess;
    private CacheAccess<String, RepoList> repoListCacheAccess;
    private ArchiveFilelistCache archiveFilelistCache;

    public static ButlerCache getInstance() {
        return instance;
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

    public void clearRepoInfoCacheAccess() {
        this.repoInfoCacheAccess.clear();
    }

    public RepoList getRepoList(String idOrName) {
        BorgRepoConfig repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(idOrName);
        //ArchiveInfo archiveInfo = BorgCommands.info(repoConfig, repoConfig.getRepo());
        RepoList repoList = repoListCacheAccess.get(repoConfig.getRepo());
        if (repoList == null) {
            repoList = BorgCommands.list(repoConfig);
            repoListCacheAccess.put(repoConfig.getRepo(), repoList);
        }
        if (repoList == null) {
            log.warn("Repo with name '" + idOrName + "' not found.");
        }
        return repoList;
    }

    public FilesystemItem[] getArchiveContent(BorgRepoConfig repoConfig, Archive archive) {
        if (archive == null || StringUtils.isBlank(archive.getArchive())) {
            return null;
        }
        FilesystemItem[] items = archiveFilelistCache.load(repoConfig, archive);
        if (items == null) {
            List<FilesystemItem> list = BorgCommands.listArchiveContent(repoConfig, archive);
            if (CollectionUtils.isNotEmpty(list)) {
                archiveFilelistCache.save(repoConfig, archive, list);
                items = list.toArray(new FilesystemItem[0]);
            }
        }
        if (items == null) {
            log.warn("Repo::archiv with name '" + repoConfig.getRepo() + "::" + archive.getArchive() + "' not found.");
        }
        return items;
    }

    public FilesystemItem[] getArchiveContent(File file) {
        return archiveFilelistCache.load(file);
    }

    public void shutdown() {
        JCS.shutdown();
    }

    public File getCacheDir() {
        return jcsCache.getCacheDir();
    }

    private ButlerCache() {
        Configuration configuration = ConfigurationHandler.getConfiguration();
        this.jcsCache = JCSCache.getInstance();
        this.repoInfoCacheAccess = jcsCache.getJCSCache("repoInfo");
        this.repoListCacheAccess = jcsCache.getJCSCache("repoList");
        this.archiveFilelistCache = new ArchiveFilelistCache(getCacheDir(), configuration.getCacheArchiveContentMaxDiscSizeMB());
    }
}
