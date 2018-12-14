package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.BorgCommands;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.Configuration;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.data.Repository;
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
    private CacheAccess<String, Repository> repoCacheAccess;
    private ArchiveFilelistCache archiveFilelistCache;

    public static ButlerCache getInstance() {
        return instance;
    }

    public Repository getRepository(String idOrName) {
        BorgRepoConfig repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(idOrName);
        Repository repository = repoCacheAccess.get(repoConfig.getRepo());
        if (repository == null ||repository.getLocation() == null) {
            repository = BorgCommands.info(repoConfig);
            repoCacheAccess.put(repoConfig.getRepo(), repository);
        }
        if (repository == null) {
            log.warn("Repo with name '" + idOrName + "' not found.");
        }
        return repository;
    }

    public List<Repository> getAllRepositories() {
        List<Repository> repositories = new ArrayList<>();
        for (BorgRepoConfig repoConfig : ConfigurationHandler.getConfiguration().getRepoConfigs()) {
            Repository repository = getRepository(repoConfig.getName());
            if (repository == null) {
                continue;
            }
            repositories.add(repository);
        }
        return repositories;
    }

    public void clearAllCaches(){
        this.repoCacheAccess.clear();
        log.info("Clearing cache with file lists of archives...");
        this.archiveFilelistCache.removeAllCacheFiles();
    }

    public void clearRepoCacheAccess() {
        log.info("Clearing repositories cache...");
        this.repoCacheAccess.clear();
    }

/*    public BorgRepoList getRepoList(String idOrName) {
        BorgRepoConfig repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(idOrName);
        //ArchiveInfo archiveInfo = BorgCommands.info(repoConfig, repoConfig.getRepo());
        BorgRepoList repoList = repoListCacheAccess.get(repoConfig.getRepo());
        if (repoList == null) {
            repoList = BorgCommands.list(repoConfig);
            repoListCacheAccess.put(repoConfig.getRepo(), repoList);
        }
        if (repoList == null) {
            log.warn("Repo with name '" + idOrName + "' not found.");
        }
        return repoList;
    }*/

    public BorgFilesystemItem[] getArchiveContent(BorgRepoConfig repoConfig, BorgArchive archive) {
        if (archive == null || StringUtils.isBlank(archive.getArchive())) {
            return null;
        }
        BorgFilesystemItem[] items = archiveFilelistCache.load(repoConfig, archive);
        if (items == null) {
            List<BorgFilesystemItem> list = BorgCommands.listArchiveContent(repoConfig, archive);
            if (CollectionUtils.isNotEmpty(list)) {
                archiveFilelistCache.save(repoConfig, archive, list);
                items = list.toArray(new BorgFilesystemItem[0]);
            }
        }
        if (items == null) {
            log.warn("Repo::archiv with name '" + repoConfig.getRepo() + "::" + archive.getArchive() + "' not found.");
        }
        return items;
    }

    public BorgFilesystemItem[] getArchiveContent(File file) {
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
        this.repoCacheAccess = jcsCache.getJCSCache("repositories");
        this.archiveFilelistCache = new ArchiveFilelistCache(getCacheDir(), configuration.getCacheArchiveContentMaxDiscSizeMB());
    }
}
