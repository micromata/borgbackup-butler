package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.BorgCommands;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.Configuration;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.data.Archive;
import de.micromata.borgbutler.data.Repository;
import de.micromata.borgbutler.json.borg.BorgArchive;
import de.micromata.borgbutler.json.borg.BorgFilesystemItem;
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
    private CacheAccess<String, Repository> repoArchivesCacheAccess;
    private CacheAccess<String, Archive> archivesCacheAccess;
    private ArchiveFilelistCache archiveFilelistCache;

    public static ButlerCache getInstance() {
        return instance;
    }

    /**
     * @param idOrName
     * @return Repository without list of archives.
     */
    public Repository getRepository(String idOrName) {
        BorgRepoConfig repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(idOrName);
        if (repoConfig != null) {
            return getRepository(repoConfig);
        }
        List<Repository> repositories = getAllRepositories();
        if (CollectionUtils.isNotEmpty(repositories)) {
            for (Repository repository : repositories) {
                if (StringUtils.equals(idOrName, repository.getName()) || StringUtils.equals(idOrName, repository.getId())) {
                    return repository;
                }
            }
        }
        log.warn("Repo with id or name '" + idOrName + "' not found.");
        return null;
    }

    /**
     * @param repoConfig
     * @return Repository without list of archives.
     */
    private Repository getRepository(BorgRepoConfig repoConfig) {
        Repository repository = repoCacheAccess.get(repoConfig.getRepo());
        if (repository == null || repository.getLocation() == null) {
            repository = BorgCommands.info(repoConfig);
            repoCacheAccess.put(repoConfig.getRepo(), repository);
        }
        if (repository == null) {
            log.warn("Repo with name '" + repoConfig.getRepo() + "' not found.");
        }
        return repository;
    }

    /**
     * @return the list of all repositories without the list of archives.
     */
    public List<Repository> getAllRepositories() {
        List<Repository> repositories = new ArrayList<>();
        for (BorgRepoConfig repoConfig : ConfigurationHandler.getConfiguration().getRepoConfigs()) {
            Repository repository = getRepository(repoConfig);
            if (repository == null) {
                continue;
            }
            repositories.add(repository);
        }
        return repositories;
    }

    public void clearAllCaches() {
        clearRepoCacheAccess();
        clearRepoArchicesCacheAccess();
        log.info("Clearing cache with file lists of archives...");
        this.archiveFilelistCache.removeAllCacheFiles();
        log.info("Clearing archives cache...");
        this.archivesCacheAccess.clear();
    }

    public void clearRepoCacheAccess() {
        log.info("Clearing repositories cache...");
        this.repoCacheAccess.clear();
    }

    public void clearRepoArchicesCacheAccess() {
        log.info("Clearing repositories cache (with included archives)...");
        this.repoArchivesCacheAccess.clear();
    }

    /**
     * @param idOrName
     * @return The repository including all archives.
     */
    public Repository getRepositoryArchives(String idOrName) {
        Repository masterRepository = getRepository(idOrName);
        if (masterRepository == null) {
            return null;
        }
        Repository repository = repoArchivesCacheAccess.get(masterRepository.getName());
        if (repository != null) {
            return repository;
        }
        BorgRepoConfig repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(masterRepository.getName());
        repository = BorgCommands.list(repoConfig, masterRepository);
        if (repository == null) return null;
        repoArchivesCacheAccess.put(repository.getName(), repository);
        return repository;
    }

    public Archive getArchive(String repoName, String archiveIdOrName) {
        return getArchive(repoName, archiveIdOrName, false);
    }

    /**
     * @param repoName
     * @param archiveIdOrName
     * @param forceReload     If true, any cache value will be ignored. Default is false.
     * @return
     */
    public Archive getArchive(String repoName, String archiveIdOrName, boolean forceReload) {
        BorgRepoConfig repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(repoName);
        if (repoConfig == null) {
            log.error("Can't find configured repo '" + repoName + "'.");
            return null;
        }
        return getArchive(repoConfig, archiveIdOrName, forceReload);
    }

    public Archive getArchive(BorgRepoConfig repoConfig, String archiveIdOrName, boolean forceReload) {
        Repository masterRepository = getRepositoryArchives(repoConfig.getRepo());
        if (masterRepository == null) {
            log.error("Repository '" + repoConfig.getRepo() + "' not found.");
            return null;
        }
        String archiveName = archiveIdOrName;
        if (CollectionUtils.isEmpty(masterRepository.getArchives())) {
            log.warn("Repository '" + repoConfig.getRepo() + "' doesn't contain archives.");
        } else {
            for (BorgArchive borgArchive : masterRepository.getArchives()) {
                if (StringUtils.equals(borgArchive.getArchive(), archiveIdOrName)
                        || StringUtils.equals(borgArchive.getId(), archiveIdOrName)) {
                    archiveName = borgArchive.getArchive();
                    break;
                }
            }
        }
        String archiveFullname = repoConfig.getRepo() + "::" + archiveName;
        if (!forceReload) {
            Archive archive = this.archivesCacheAccess.get(archiveFullname);
            if (archive != null) {
                return archive;
            }
        }
        Archive archive = BorgCommands.info(repoConfig, archiveName, masterRepository);
        if (archive != null)
            this.archivesCacheAccess.put(archiveFullname, archive);
        return archive;
    }

    public BorgFilesystemItem[] getArchiveContent(BorgRepoConfig repoConfig, BorgArchive archive) {
        if (archive == null || StringUtils.isBlank(archive.getArchive())) {
            return null;
        }
        BorgFilesystemItem[] items = archiveFilelistCache.load(repoConfig, archive);
        if (items == null) {
            List<BorgFilesystemItem> list = BorgCommands.listArchiveContent(repoConfig, archive.getArchive());
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
        this.repoArchivesCacheAccess = jcsCache.getJCSCache("repositoriesArchives");
        this.archivesCacheAccess = jcsCache.getJCSCache("archives");
        this.archiveFilelistCache = new ArchiveFilelistCache(getCacheDir(), configuration.getCacheArchiveContentMaxDiscSizeMB());
    }
}
