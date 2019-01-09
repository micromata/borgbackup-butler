package de.micromata.borgbutler.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private static final String SERIALIZATION_ID_STRING = "kryo 5.0.0-RC1";
    private static Logger log = LoggerFactory.getLogger(ArchiveFilelistCache.class);
    private static final String CACHE_ARCHIVE_LISTS_BASENAME = "archive-content-";
    private static final String CACHE_FILE_GZIP_EXTENSION = ".gz";
    private static final BigDecimal THOUSAND = new BigDecimal(1000);
    private File cacheDir;
    private int cacheArchiveContentMaxDiscSizeMB;
    private long FILES_EXPIRE_TIME = 7 * 24 * 3660 * 1000; // Expires after 7 days.
    // For avoiding concurrent writing of same files (e. g. after the user has pressed a button twice).
    private Set<File> savingFiles = new HashSet<>();

    ArchiveFilelistCache(File cacheDir, int cacheArchiveContentMaxDiscSizeMB) {
        this.cacheDir = cacheDir;
        this.cacheArchiveContentMaxDiscSizeMB = cacheArchiveContentMaxDiscSizeMB;
    }

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
            }
            log.info("Saving archive content as file list: " + file.getAbsolutePath());

            Kryo kryo = createKryo();
            Deque<BorgFilesystemItem> stack = new ArrayDeque<>();
            try (Output outputStream = new Output(new GzipCompressorOutputStream(new FileOutputStream(file)))) {
                kryo.writeObject(outputStream, SERIALIZATION_ID_STRING);
                kryo.writeObject(outputStream, filesystemItems.size());
                Iterator<BorgFilesystemItem> it = filesystemItems.iterator();
                while (it.hasNext()) {
                    BorgFilesystemItem item = it.next();
                    kryo.writeObject(outputStream, item);
                }
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
        return load(file, archive, filter);
    }

    /**
     * @param file
     * @param filter If given, only file items matching this filter are returned.
     * @return
     */
    public List<BorgFilesystemItem> load(File file, FileSystemFilter filter) {
        return load(file, null, filter);
    }

    /**
     * @param file
     * @param archive Only for storing file system items as recent (may-be null)
     * @param filter  If given, only file items matching this filter are returned.
     * @return
     */
    public List<BorgFilesystemItem> load(File file, Archive archive, FileSystemFilter filter) throws RuntimeException {
        if (!file.exists()) {
            log.error("File '" + file.getAbsolutePath() + "' doesn't exist. Can't get archive content files.");
            return null;
        }
        log.info("Loading archive content as file list from: " + file.getAbsolutePath());
        try {
            // Set last modified time of file:
            Files.setAttribute(file.toPath(), "lastModifiedTime", FileTime.fromMillis(System.currentTimeMillis()));
        } catch (IOException ex) {
            log.error("Can't set lastModifiedTime on file '" + file.getAbsolutePath() + "'. Pruning old cache files may not work.");
        }
        long millis = System.currentTimeMillis();
        // GZipCompressorInputStream buffers already, no BufferedInputReader needed.
        Kryo kryo = createKryo();
        List<BorgFilesystemItem> list = null;
        try (Input inputStream = new Input(new GzipCompressorInputStream(new FileInputStream(file)))) {
            String serializationId = kryo.readObject(inputStream, String.class);
            if (!SERIALIZATION_ID_STRING.equals(serializationId)) {
                log.info("Incompatible archive cache file format. Expected id '" + SERIALIZATION_ID_STRING + "', but received: '" + serializationId
                        + "'. OK, trying to get the data from Borg again.");
                return null;
            }
            list = ButlerCacheHelper.readAndMatchInputStream(kryo, inputStream, filter);
        } catch (Exception ex) {
            log.error("Error while reading file list '" + file.getAbsolutePath() + "': " + ex.getMessage() + ". OK, trying to get the data from Borg again.");
            return null;
        }
        BigDecimal bd = new BigDecimal(System.currentTimeMillis() - millis).divide(THOUSAND, 1, RoundingMode.HALF_UP);
        log.info("Loading of " + String.format("%,d", list.size()) + " file system items done in " + bd + " seconds.");
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

    public void deleteCachFile(Repository repository, Archive archive) {
        File file = getFile(repository, archive);
        if (file.exists()) {
            log.info("Deleting cache file: " + file.getAbsolutePath());
            file.delete();
        } else {
            log.info("Can't delete requested file because it doesn't exist (anymore): " + file.getAbsolutePath());
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

    private boolean isCacheFile(File file) {
        return file.getName().startsWith(CACHE_ARCHIVE_LISTS_BASENAME);
    }


    private Kryo createKryo() {
        Kryo kryo = new Kryo();
        kryo.register(BorgFilesystemItem.class, 9);
        kryo.register(BorgFilesystemItem.DiffStatus.class, 10);
        kryo.setMaxDepth(10);
        kryo.setWarnUnregisteredClasses(true);
        kryo.setReferences(false);
        return kryo;
    }
}

