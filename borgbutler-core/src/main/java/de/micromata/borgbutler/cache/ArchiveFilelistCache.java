package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.data.Archive;
import de.micromata.borgbutler.data.FileSystemFilter;
import de.micromata.borgbutler.data.Repository;
import de.micromata.borgbutler.json.borg.BorgFilesystemItem;
import de.micromata.borgbutler.utils.ReplaceUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;

/**
 * Cache for storing complete file lists of archives as gzipped files (using Java standard serialization for
 * fastest access).
 * <br>
 * A file list (archive content) with over million file system items is over 100MB large (uncompressed).
 * The compression is also useful for faster reading from the filesystem.
 */
class ArchiveFilelistCache {
    private static Logger log = LoggerFactory.getLogger(ArchiveFilelistCache.class);
    private static final String CACHE_ARCHIVE_LISTS_BASENAME = "archive-content-";
    private static final String CACHE_FILE_GZIP_EXTENSION = ".gz";
    private File cacheDir;
    private int cacheArchiveContentMaxDiscSizeMB;
    private long FILES_EXPIRE_TIME = 7 * 24 * 3660 * 1000; // Expires after 7 days.
    // For avoiding concurrent writing of same files (e. g. after the user has pressed a button twice).
    private Set<File> savingFiles = new HashSet<>();

    public void save(BorgRepoConfig repoConfig, Archive archive, List<BorgFilesystemItem> filesystemItems) {
        if (CollectionUtils.isEmpty(filesystemItems)) {
            return;
        }
        File file = getFile(repoConfig, archive);
        try {
            synchronized (savingFiles) {
                if (savingFiles.contains(file)) {
                    // File will already be written. This occurs if the user pressed a button twice.
                    log.info("Don't write the archive content twice.");
                    return;
                }
                savingFiles.add(file);
                Collections.sort(filesystemItems); // Sort by path.
            }
            log.info("Saving archive content as file list: " + file.getAbsolutePath());
            try (ObjectOutputStream outputStream = new ObjectOutputStream(new BufferedOutputStream(new GzipCompressorOutputStream(new FileOutputStream(file))))) {
                outputStream.writeObject(filesystemItems.size());
                for (BorgFilesystemItem item : filesystemItems) {
                    outputStream.writeObject(item);
                }
                outputStream.writeObject("EOF");
            } catch (IOException ex) {
                log.error("Error while writing file list '" + file.getAbsolutePath() + "': " + ex.getMessage(), ex);
            }
        } finally {
            synchronized (savingFiles) {
                savingFiles.remove(file);
            }
        }
        log.info("Saving done.");
    }

    /**
     * @param repository
     * @param archive
     * @return true, if the content of the archive is already cached, otherwise false.
     */
    public boolean contains(Repository repository, Archive archive) {
        File file = getFile(repository, archive);
        return file.exists();
    }

    /**
     * Calls {@link #load(BorgRepoConfig, Archive, FileSystemFilter)} with filter null.
     *
     * @param repoConfig
     * @param archive
     * @return
     */
    public List<BorgFilesystemItem> load(BorgRepoConfig repoConfig, Archive archive) {
        return load(repoConfig, archive, null);
    }


    /**
     * Will load and touch the archive file if exist. The file will be touched (last modified time will be set to now)
     * for pruning oldest cache files. The last modified time will be the time of the last usage.
     *
     * @param repoConfig
     * @param archive
     * @param filter     If given, only file items matching this filter are returned.
     * @return
     */
    public List<BorgFilesystemItem> load(BorgRepoConfig repoConfig, Archive archive, FileSystemFilter filter) {
        File file = getFile(repoConfig, archive);
        if (!file.exists()) {
            return null;
        }
        return load(file, filter);
    }

    /**
     * @param file
     * @param filter If given, only file items matching this filter are returned.
     * @return
     */
    public List<BorgFilesystemItem> load(File file, FileSystemFilter filter) {
        if (!file.exists()) {
            log.error("File '" + file.getAbsolutePath() + "' doesn't exist. Can't get archive content files.");
            return null;
        }
        log.info("Loading archive content as file list from: " + file.getAbsolutePath());
        List<BorgFilesystemItem> list = null;
        try {
            // Set last modified time of file:
            Files.setAttribute(file.toPath(), "lastModifiedTime", FileTime.fromMillis(System.currentTimeMillis()));
        } catch (IOException ex) {
            log.error("Can't set lastModifiedTime on file '" + file.getAbsolutePath() + "'. Pruning old cache files may not work.");
        }
        try (ObjectInputStream inputStream = new ObjectInputStream(new BufferedInputStream(new GzipCompressorInputStream(new FileInputStream(file))))) {
            Object obj = inputStream.readObject();
            if (!(obj instanceof Integer)) {
                log.error("Can't load archive content. Integer expected, but received: " + obj.getClass());
                return null;
            }
            int size = (Integer) obj;
            list = new ArrayList<>();
            int fileNumber = -1;
            for (int i = 0; i < size; i++) {
                ++fileNumber;
                obj = inputStream.readObject();
                if (obj instanceof BorgFilesystemItem) {
                    BorgFilesystemItem item = (BorgFilesystemItem) obj;
                    item.setFileNumber(fileNumber);
                    if (filter == null || filter.matches(item)) {
                        list.add(item);
                        if (filter != null && filter.isFinished()) break;
                    }
                } else {
                    log.error("Can't load archive content. FilesystemItem expected, but received: "
                            + (obj != null ? obj.getClass() : "null")
                            + " at position " + i + ".");
                    return null;
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            log.error("Error while reading file list '" + file.getAbsolutePath() + "': " + ex.getMessage(), ex);
        }
        Collections.sort(list); // Sort by path (if archive list order wasn't correct).
        log.info("Loading done.");
        if (filter != null) {
            return filter.reduce(list);
        }
        return list;
    }

    /**
     * Deletes archive contents older than 7 days and deletes the oldest archive contents if the max cache size is
     * exceeded. The last modified time of a file is equals to the last usage by
     * {@link #load(BorgRepoConfig, Archive, FileSystemFilter)}.
     */
    public void cleanUp() {
        File[] files = cacheDir.listFiles();
        long currentMillis = System.currentTimeMillis();
        for (File file : files) {
            try {
                if (!file.exists() || !isCacheFile(file)) continue;
                // Get last modified time of file:
                FileTime time = Files.readAttributes(file.toPath(), BasicFileAttributes.class).lastModifiedTime();
                if (currentMillis - FILES_EXPIRE_TIME > time.toMillis()) {
                    log.info("Delete expired cache file (last usage " + time + " older than 7 days): " + file.getAbsolutePath());
                    file.delete();
                }
            } catch (IOException ex) {
                log.error("Can't get last modified time from cache files (ignore file '" + file.getAbsolutePath() + "'): " + ex.getMessage(), ex);
            }
        }
        int sizeInMB = getCacheDiskSizeInMB(files);
        if (sizeInMB > cacheArchiveContentMaxDiscSizeMB) {
            log.info("Maximum size of cache files exceeded (" + sizeInMB + "MB > " + cacheArchiveContentMaxDiscSizeMB
                    + "MB). Deleting the old ones (with the oldest usage)...");
        } else {
            // Nothing to clean up anymore.
            return;
        }
        SortedMap<FileTime, File> sortedFiles = new TreeMap<>();
        for (File file : files) {
            if (!file.exists() || !isCacheFile(file)) continue;
            try {
                // Get last modified time of file:
                FileTime time = Files.readAttributes(file.toPath(), BasicFileAttributes.class).lastModifiedTime();
                sortedFiles.put(time, file);
            } catch (IOException ex) {
                log.error("Can't get last modified time from cache files (ignore file '" + file.getAbsolutePath() + "'): " + ex.getMessage(), ex);
            }
        }
        for (Map.Entry<FileTime, File> entry : sortedFiles.entrySet()) {
            FileTime time = entry.getKey();
            File file = entry.getValue();
            if (!file.exists() || !isCacheFile(file)) continue;
            log.info("Deleting cache file (last usage " + time + "): " + file.getAbsolutePath());
            file.delete();
            int newSizeInMB = getCacheDiskSizeInMB(files);
            if (newSizeInMB < cacheArchiveContentMaxDiscSizeMB) {
                log.info("New cache size is " + newSizeInMB + "MB. (" + (sizeInMB - newSizeInMB) + "MB deleted.)");
                break;
            }
        }
    }

    public int getCacheDiskSizeInMB() {
        return getCacheDiskSizeInMB(cacheDir.listFiles());
    }

    private int getCacheDiskSizeInMB(File[] files) {
        int sizeInMB = 0;
        for (File file : files) {
            if (!file.exists()) continue;
            if (!isCacheFile(file)) continue;
            sizeInMB += (int) (file.length() / 1048576); // In MB
        }
        return sizeInMB;
    }

    public void removeAllCacheFiles() {
        File[] files = cacheDir.listFiles();
        for (File file : files) {
            if (isCacheFile(file)) {
                log.info("Deleting cache file: " + file.getAbsolutePath());
                file.delete();
            }
        }
    }

    File getFile(Repository repository, Archive archive) {
        return getFile(repository.getName(), archive);
    }

    File getFile(BorgRepoConfig repoConfig, Archive archive) {
        return getFile(repoConfig.getRepo(), archive);
    }

    private File getFile(String repo, Archive archive) {
        return new File(cacheDir, ReplaceUtils.encodeFilename(CACHE_ARCHIVE_LISTS_BASENAME + archive.getTime()
                        + "-" + repo + "-" + archive.getName() + CACHE_FILE_GZIP_EXTENSION,
                true));
    }

    ArchiveFilelistCache(File cacheDir, int cacheArchiveContentMaxDiscSizeMB) {
        this.cacheDir = cacheDir;
        this.cacheArchiveContentMaxDiscSizeMB = cacheArchiveContentMaxDiscSizeMB;
    }

    private boolean isCacheFile(File file) {
        return file.getName().startsWith(CACHE_ARCHIVE_LISTS_BASENAME);
    }
}

