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
import java.util.List;

class ArchiveFilelistCache {
    private static Logger log = LoggerFactory.getLogger(ArchiveFilelistCache.class);
    private static final String CACHE_ARCHIVE_LISTS_BASENAME = "archive-content-";
    private static final String CACHE_FILE_GZIP_EXTENSION = "gz";
    private File cacheDir;

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
        for (File file : files) {
            if (isCacheFile(file)) {
                log.info("Processing cache file: " + file.getAbsolutePath());
                //file.delete();
            }
        }
    }

    File getFile(BorgRepoConfig repoConfig, Archive archive) {
        return new File(cacheDir, ReplaceUtils.encodeFilename(CACHE_ARCHIVE_LISTS_BASENAME + archive.getTime()
                + "-" + repoConfig.getRepo() + "-" + archive.getArchive() + ".gz", true));
    }

    ArchiveFilelistCache(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    private boolean isCacheFile(File file) {
        return file.getName().startsWith(CACHE_ARCHIVE_LISTS_BASENAME);
    }
}

