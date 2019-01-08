package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.BorgCommands;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.Configuration;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.data.Archive;
import de.micromata.borgbutler.data.ArchiveShortInfo;
import de.micromata.borgbutler.data.FileSystemFilter;
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
import java.util.Iterator;
import java.util.List;

/**
 * Caches all borg objects such as repository information, archive and archive content. Therefore a fluent work
 * is possible. Without caching, working with BorgButler especially for remote backups would be a time consuming mess.
 * <br>
 * For most objects JCS is used. For file lists (up to million of files) an own implementation is used because JCS
 * isn't recommended for caching a very large number of objects or very large objects.
 */
public class ButlerCache {
    private Logger log = LoggerFactory.getLogger(ButlerCache.class);
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
            if (repository != null) {
                repoCacheAccess.put(repoConfig.getRepo(), repository);
            }
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
            updateArchivesCacheStatusAndShortInfos(repository);
            return repository;
        }
        BorgRepoConfig repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(repository.getName());
        BorgCommands.list(repoConfig, repository);
        updateArchivesCacheStatusAndShortInfos(repository);
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
            updateArchivesCacheStatusAndShortInfos(repository);
            return archive;
        }
        BorgCommands.info(repoConfig, archive, repository);
        updateArchivesCacheStatusAndShortInfos(repository);
        return archive;
    }

    public Archive getArchive(String archiveId) {
        for (Repository repository : getAllRepositories()) {
            if (repository.getArchives() != null) {
                for (Archive archive : repository.getArchives()) {
                    if (StringUtils.equals(archiveId, archive.getId())) {
                        return archive;
                    }
                }
            }
        }
        log.error("Archive with id '" + archiveId + "' not found. May-be not yet loaded into the cache.");
        return null;
    }

    /**
     * Updates for all archives of the given repository the cache status ({@link Archive#isFileListAlreadyCached()} and
     * updates also the list of {@link ArchiveShortInfo} for all archives of the given repository.
     *
     * @param repository
     */
    private void updateArchivesCacheStatusAndShortInfos(Repository repository) {
        if (repository == null || repository.getArchives() == null) {
            return;
        }
        List<ArchiveShortInfo> archiveInfoList = new ArrayList<>();
        for (Archive archive : repository.getArchives()) {
            archive.setFileListAlreadyCached(archiveFilelistCache.contains(repository, archive));
            archiveInfoList.add(new ArchiveShortInfo(archive));
        }
        for (Archive archive : repository.getArchives()) {
            // ArchiveInfoList for comparing current archives with one of all other archives.
            archive.setArchiveShortInfoList(archiveInfoList);
        }
    }

    /**
     * @param archiveId
     * @param forceLoad If false, the file list will only get if not yet loaded.
     * @param filter    If not null, the result will be filtered.
     * @return
     */
    public List<BorgFilesystemItem> getArchiveContent(String archiveId, boolean forceLoad, FileSystemFilter filter) {
        Archive archive = getArchive(archiveId);
        if (archive == null) {
            log.error("Can't find archive with id '" + archiveId + "'. May-be it doesn't exist or the archives of the target repository aren't yet loaded.");
            return null;
        }
        BorgRepoConfig repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(archive.getRepoId());
        return getArchiveContent(repoConfig, archive, forceLoad, filter);
    }

    /**
     * @param repoConfig
     * @param archive
     * @return
     */
    public List<BorgFilesystemItem> getArchiveContent(BorgRepoConfig repoConfig, Archive archive) {
        return getArchiveContent(repoConfig, archive, true, null);
    }

    /**
     * @param repoConfig
     * @param archive
     * @param filter     If given, only the items matching this filter are returned..
     * @return
     */
    public List<BorgFilesystemItem> getArchiveContent(BorgRepoConfig repoConfig, Archive archive, boolean forceLoad,
                                                      FileSystemFilter filter) {
        if (archive == null || StringUtils.isBlank(archive.getName())) {
            return null;
        }
        synchronized (archive) {
            List<BorgFilesystemItem> items = archiveFilelistCache.load(repoConfig, archive, filter);
            if (items == null && forceLoad) {
                List<BorgFilesystemItem> list = BorgCommands.listArchiveContent(repoConfig, archive);
                if (CollectionUtils.isNotEmpty(list)) {
                    archiveFilelistCache.save(repoConfig, archive, list);
                    items = new ArrayList<>();
                    int fileNumber = -1;
                    Iterator<BorgFilesystemItem> it = list.iterator(); // Don't use for-each (ConcurrentModificationException)
                    while (it.hasNext()) {
                        BorgFilesystemItem item = it.next();
                        ++fileNumber;
                        item.setFileNumber(fileNumber);
                        if (filter == null || filter.matches(item)) {
                            items.add(item);
                            if (filter != null && filter.isFinished()) break;
                        }
                    }
                    if (filter != null) {
                        items = filter.reduce(items);
                    }
                }
            }
            if (items == null && forceLoad) {
                log.warn("Repo::archiv with name '" + archive.getBorgIdentifier() + "' not found or job was cancelled.");
            }
            return items;
        }
    }

    public List<BorgFilesystemItem> getArchiveContent(File file) {
        return archiveFilelistCache.load(file, null);
    }

    public void deleteCachedArchiveContent(String repoIdOrName, String archiveIdOrName) {
        Repository repository = getRepository(repoIdOrName);
        if (repository == null) {
            log.warn("Can't delete archive content cache file, repository not found: " + repoIdOrName);
            return;
        }
        Archive archive = getArchive(repoIdOrName, archiveIdOrName);
        if (archive == null) {
            log.warn("Can't delete archive content cache file, archive not found: " + repoIdOrName);
            return;
        }
        archiveFilelistCache.deleteCachFile(repository, archive);
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
        this.archiveFilelistCache = new ArchiveFilelistCache(getCacheDir(), configuration.getMaxArchiveContentCacheCapacityMb());
        // Assign the repo ids to the repo config objects:
        getAllRepositories();
    }
}
