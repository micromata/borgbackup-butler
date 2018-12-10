package de.micromata.borgbutler.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.micromata.borgbutler.BorgCommands;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.json.borg.*;
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
    public static final String CACHE_DIR_NAME = "caches";
    private static ButlerCache instance = new ButlerCache();

    private JCSCache jcsCache = JCSCache.getInstance();
    private CacheAccess<String, RepoInfo> repoInfoCacheAccess;
    private CacheAccess<String, RepoList> repoListCacheAccess;
    private CacheAccess<String, List<FilesystemItem>> archiveContentCacheAccess;

    @JsonIgnore
    private File cacheDir;

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

    public List<FilesystemItem> getArchiveContent(BorgRepoConfig repoConfig, Archive archive) {
        if (archive == null || StringUtils.isBlank(archive.getArchive())) {
            return null;
        }
        String repoArchiveId = getRepoArchiveId(repoConfig.getRepo(), archive.getId());
        List<FilesystemItem> content = archiveContentCacheAccess.get(repoArchiveId);
        if (content == null) {
            content = BorgCommands.listArchiveContent(repoConfig, archive);
            archiveContentCacheAccess.put(repoArchiveId, content);
            archiveContentCacheAccess.getStatistics();
        }
        log.info("archiveContentCacheAccess.stats: " + this.archiveContentCacheAccess.getStats());
        if (content == null) {
            log.warn("Repo::archiv with name '" + repoConfig.getRepo() + "::" + archive.getArchive() + "' not found.");
        }
        return content;
    }

    public String getRepoArchiveId(String repo, String archiveId) {
        return repo + "::" + archiveId;
    }

    public void shutdown() {
        log.info("archiveContentCacheAccess.stats: " + this.archiveContentCacheAccess.getStats());
        JCS.shutdown();
    }

    private ButlerCache() {
        cacheDir = new File(ConfigurationHandler.getInstance().getWorkingDir(), CACHE_DIR_NAME);
        if (!cacheDir.exists()) {
            log.info("Creating cache dir: " + cacheDir.getAbsolutePath());
            cacheDir.mkdir();
        }
        this.repoInfoCacheAccess = jcsCache.getJCSCache("repoInfo");
        this.repoListCacheAccess = jcsCache.getJCSCache("repoList");
        this.archiveContentCacheAccess = jcsCache.getJCSCache("archiveContent");
    }
}
