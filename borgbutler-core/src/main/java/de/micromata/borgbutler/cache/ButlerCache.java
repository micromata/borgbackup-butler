package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.BorgCommands;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.Configuration;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.data.Archive;
import de.micromata.borgbutler.data.Repository;
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
    private ArchiveFilelistCache archiveFilelistCache;
    private int notYetLoadedIdCounter = 1;

    public static ButlerCache getInstance() {
        return instance;
    }

    /**
     * @param idOrName
     * @return Repository.
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
     * @return Repository.
     */
    private Repository getRepository(BorgRepoConfig repoConfig) {
        Repository repository = null;
        try {
            repository = repoCacheAccess.get(repoConfig.getRepo());
        } catch (Exception ex) {
            log.warn("Error while deserializing repository (maybe data format was changed). Reloading...");
        }
        if (repository == null || repository.getLocation() == null) {
            repository = BorgCommands.info(repoConfig);
            repoCacheAccess.put(repoConfig.getRepo(), repository);
        }
        if (repository == null) {
            log.warn("Repo with name '" + repoConfig.getRepo() + "' not found.");
        } else {
            repoConfig.setId(repository.getId());
        }
        return repository;
    }

    /**
     * @return the list of all repositories.
     */
    public List<Repository> getAllRepositories() {
        List<Repository> repositories = new ArrayList<>();
        for (BorgRepoConfig repoConfig : ConfigurationHandler.getConfiguration().getRepoConfigs()) {
            Repository repository = repoCacheAccess.get(repoConfig.getRepo());
            if (repository == null) {
                if (repoConfig.getId() == null) {
                    // Temporary id:
                    repoConfig.setId("not_yet_loaded_" + notYetLoadedIdCounter++);
                }
                repository = new Repository()
                        .setDisplayName(repoConfig.getDisplayName())
                        .setName(repoConfig.getRepo())
                        .setId(repoConfig.getId());
            } else if (repoConfig.getId() == null) {
                // On initial call, the repo id is not assigned to BorgRepoConfig for cached repositories:
                repoConfig.setId(repository.getId());
            }
            repositories.add(repository);
        }
        return repositories;
    }

    public void clearAllCaches() {
        clearRepoCacheAccess();
        log.info("Clearing cache with file lists of archives...");
        this.archiveFilelistCache.removeAllCacheFiles();
    }

    public void clearRepoCacheAccess() {
        log.info("Clearing repositories cache...");
        this.repoCacheAccess.clear();
    }

    /**
     * @param idOrName
     * @return The repository (ensures that the list of archives is loaded).
     */
    public Repository getRepositoryArchives(String idOrName) {
        Repository repository = getRepository(idOrName);
        if (repository == null) {
            return null;
        }
        if (repository.isArchivesLoaded()) {
            return repository;
        }
        BorgRepoConfig repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(repository.getName());
        BorgCommands.list(repoConfig, repository);
        return repository;
    }

    public Archive getArchive(String repoName, String archiveIdOrName) {
        return getArchive(repoName, archiveIdOrName, false);
    }

    /**
     * @param repoIdOrName
     * @param archiveIdOrName
     * @param forceReload     If true, any cache value will be ignored. Default is false.
     * @return
     */
    public Archive getArchive(String repoIdOrName, String archiveIdOrName, boolean forceReload) {
        BorgRepoConfig repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(repoIdOrName);
        if (repoConfig == null) {
            log.error("Can't find configured repo '" + repoIdOrName + "'.");
            return null;
        }
        return getArchive(repoConfig, archiveIdOrName, forceReload);
    }

    public Archive getArchive(BorgRepoConfig repoConfig, String archiveIdOrName, boolean forceReload) {
        Repository repository = getRepositoryArchives(repoConfig.getRepo());
        if (repository == null) {
            log.error("Repository '" + repoConfig.getRepo() + "' not found.");
            return null;
        }
        Archive archive = null;
        if (CollectionUtils.isEmpty(repository.getArchives())) {
            log.warn("Repository '" + repoConfig.getRepo() + "' doesn't contain archives.");
        } else {
            for (Archive arch : repository.getArchives()) {
                if (StringUtils.equals(arch.getName(), archiveIdOrName)
                        || StringUtils.equals(arch.getId(), archiveIdOrName)) {
                    archive = arch;
                    break;
                }
            }
        }
        if (archive == null) {
            log.error("Archive with id or name '" + archiveIdOrName + "' not found for repo '" + repoConfig.getRepo()
                    + "'.");
            return null;
        }
        if (!forceReload && archive.hasInfoData()) {
            // borg info archive was already called.
            return archive;
        }
        BorgCommands.info(repoConfig, archive, repository);
        return archive;
    }

    public List<BorgFilesystemItem> getArchiveContent(String archiveId) {
        return getArchiveContent(archiveId, true, -1);
    }

    /**
     * @param archiveId
     * @param forceLoad If false, the file list will only get if not yet loaded.
     * @param maxSize
     * @return
     */
    public List<BorgFilesystemItem> getArchiveContent(String archiveId, boolean forceLoad, int maxSize) {
        Archive archive = null;
        outerLoop:
        for (Repository repository : getAllRepositories()) {
            if (repository.getArchives() != null) {
                for (Archive arch : repository.getArchives()) {
                    if (StringUtils.equals(archiveId, arch.getId())) {
                        archive = arch;
                        break outerLoop;
                    }
                }
            }
        }
        if (archive == null) {
            log.error("Can't find archive with id '" + archiveId + "'. May-be it doesn't exist or the archives of the target repository aren't yet loaded.");
            return null;
        }
        BorgRepoConfig repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(archive.getRepoId());
        return getArchiveContent(repoConfig, archive, forceLoad, maxSize);
    }

    /**
     * @param repoConfig
     * @param archive
     * @return
     */
    public List<BorgFilesystemItem> getArchiveContent(BorgRepoConfig repoConfig, Archive archive) {
        return getArchiveContent(repoConfig, archive, true, -1);
    }

    /**
     * @param repoConfig
     * @param archive
     * @param maxSize    Max result size (default is -1 meaning all).
     * @return
     */
    public List<BorgFilesystemItem> getArchiveContent(BorgRepoConfig repoConfig, Archive archive, boolean forceLoad, int maxSize) {
        if (archive == null || StringUtils.isBlank(archive.getName())) {
            return null;
        }
        List<BorgFilesystemItem> items = archiveFilelistCache.load(repoConfig, archive, maxSize);
        if (items == null && forceLoad) {
            List<BorgFilesystemItem> list = BorgCommands.listArchiveContent(repoConfig, archive.getName());
            if (CollectionUtils.isNotEmpty(list)) {
                archiveFilelistCache.save(repoConfig, archive, list);
                items = new ArrayList<>();
                int i = 0;
                for (BorgFilesystemItem item : list) {
                    if (++i > maxSize) break;
                    items.add(item);
                }
            }
        }
        if (items == null && forceLoad) {
            log.warn("Repo::archiv with name '" + archive.getBorgIdentifier() + "' not found.");
        }
        return items;
    }

    public List<BorgFilesystemItem> getArchiveContent(File file) {
        return archiveFilelistCache.load(file, -1);
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
