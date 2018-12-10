package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.json.borg.Archive;
import de.micromata.borgbutler.json.borg.FilesystemItem;
import de.micromata.borgbutler.utils.ReplaceUtils;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

class ArchiveFilelistCache {
    private static Logger log = LoggerFactory.getLogger(ArchiveFilelistCache.class);
    private static final String CACHE_ARCHIVE_LISTS_BASENAME = "archive-content-";
    private static final String CACHE_FILE_GZIP_EXTENSION = "gz";
    private File cacheDir;
    private int cacheArchiveContentMaxDiscSizeMB;
    private long FILES_EXPIRE_TIME = 5 * 24 * 3660 * 1000; // Expires after 5 days.

    @Getter
    private Archive archive;
    private List<FilesystemItem> content;

    public void save(BorgRepoConfig repoConfig, Archive archive, List<FilesystemItem> filesystemItems) {
        File file = getFile(repoConfig, archive);
        if (CollectionUtils.isEmpty(filesystemItems)) {
            return;
        }
        log.info("Saving archive content as file list: " + file.getAbsolutePath());
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new BufferedOutputStream(new GzipCompressorOutputStream(new FileOutputStream(file))))) {
            outputStream.writeObject(filesystemItems.size());
            for (FilesystemItem item : filesystemItems) {
                outputStream.writeObject(item);
            }
            outputStream.writeObject("EOF");
        } catch (IOException ex) {
            log.error("Error while writing file list '" + file.getAbsolutePath() + "': " + ex.getMessage(), ex);
        }
        log.info("Saving done.");
    }

    public FilesystemItem[] load(BorgRepoConfig repoConfig, Archive archive) {
        File file = getFile(repoConfig, archive);
        if (!file.exists()) {
            return null;
        }
        log.info("Loading archive content as file list from: " + file.getAbsolutePath());
        FilesystemItem[] list = null;
        try (ObjectInputStream inputStream = new ObjectInputStream(new BufferedInputStream(new GzipCompressorInputStream(new FileInputStream(file))))) {
            Object obj = inputStream.readObject();
            if (!(obj instanceof Integer)) {
                log.error("Can't load archive content. Integer expected, but received: " + obj.getClass());
                return null;
            }
            int size = (Integer) obj;
            list = new FilesystemItem[size];
            for (int i = 0; i < size; i++) {
                obj = inputStream.readObject();
                if (obj instanceof FilesystemItem) {
                    list[i] = (FilesystemItem) obj;
                } else {
                    log.error("Can't load archive content. FilesystemItem expected, but received: " + obj.getClass()
                            + " at position " + i + ".");
                    return null;
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            log.error("Error while reading file list '" + file.getAbsolutePath() + "': " + ex.getMessage(), ex);
        }
        log.info("Loading done.");
        return list;
    }

    /**
     * Deletes archive contents older than 7 days and deletes the oldest archive contents if the max cache size is
     * exceeded.
     */
    public void cleanUp() {
        File[] files = cacheDir.listFiles();
        long currentMillis = System.currentTimeMillis();
        for (File file : files) {
            try {
                if (!file.exists() || !isCacheFile(file)) continue;
                // Get last access time of file:
                FileTime time = Files.readAttributes(file.toPath(), BasicFileAttributes.class).lastAccessTime();
                if (currentMillis - FILES_EXPIRE_TIME > time.toMillis()) {
                    log.info("Delete old cache file (last access " + time + " older than 5 days): " + file.getAbsolutePath());
                    file.delete();
                }
            } catch (IOException ex) {
                log.error("Can't get last accesstime from cache files (ignore file '" + file.getAbsolutePath() + "'): " + ex.getMessage(), ex);
            }
        }
        int sizeInMB = getCacheDiskSizeInMB(files);
        if (sizeInMB > cacheArchiveContentMaxDiscSizeMB) {
            log.info("Maximum size of cache files exceeded (" + sizeInMB + "MB > " + cacheArchiveContentMaxDiscSizeMB
                    + "MB). Deleting the old ones (with the oldest access)...");
        }
        SortedMap<FileTime, File> sortedFiles = new TreeMap<>();
        for (File file : files) {
            if (!file.exists() || !isCacheFile(file)) continue;
            try {
                // Get last access time of file:
                FileTime time = Files.readAttributes(file.toPath(), BasicFileAttributes.class).lastAccessTime();
                sortedFiles.put(time, file);
            } catch (IOException ex) {
                log.error("Can't get last accesstime from cache files (ignore file '" + file.getAbsolutePath() + "'): " + ex.getMessage(), ex);
            }
        }
        for (Map.Entry<FileTime, File> entry : sortedFiles.entrySet()) {
            FileTime time = entry.getKey();
            File file = entry.getValue();
            if (!file.exists() || !isCacheFile(file)) continue;
            log.info("Deleting cache file (last access " + time + "): " + file.getAbsolutePath());
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

    File getFile(BorgRepoConfig repoConfig, Archive archive) {
        return new File(cacheDir, ReplaceUtils.encodeFilename(CACHE_ARCHIVE_LISTS_BASENAME + archive.getTime()
                + "-" + repoConfig.getRepo() + "-" + archive.getArchive() + ".gz", true));
    }

    ArchiveFilelistCache(File cacheDir, int cacheArchiveContentMaxDiscSizeMB) {
        this.cacheDir = cacheDir;
        this.cacheArchiveContentMaxDiscSizeMB = cacheArchiveContentMaxDiscSizeMB;
    }

    private boolean isCacheFile(File file) {
        return file.getName().startsWith(CACHE_ARCHIVE_LISTS_BASENAME);
    }
}

