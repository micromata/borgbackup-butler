package de.micromata.borgbutler.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.micromata.borgbutler.config.Definitions;
import de.micromata.borgbutler.json.JsonUtils;
import lombok.Getter;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Date;

public abstract class AbstractCache {
    private static Logger log = LoggerFactory.getLogger(AbstractCache.class);
    private static final String CACHE_FILE_PREFIX = "cache-";
    private static final String CACHE_FILE_EXTENSION = "json";
    private static final String CACHE_FILE_ZIP_EXTENSION = ".gz";

    /**
     * INITIAL - on startup (not yet read), DIRTY - modifications not written, SAVED - content written to file.
     */
    protected enum STATE {INITIAL, DIRTY, SAVED}

    @JsonIgnore
    protected File cacheFile;
    @JsonIgnore
    @Getter
    private boolean compress;
    @Getter
    @JsonIgnore
    private STATE state = STATE.INITIAL;
    @Getter
    private Date lastModified;
    @Getter
    private Date created;

    /**
     * Removes all entries (doesn't effect the cache files!).
     */
    public void clear() {
        state = STATE.DIRTY;
    }

    /**
     * Calls {@link #clear()} and removes the cache files. Therefore a new creation of this cache is forced.
     */
    public void clearAndReset() {
        synchronized (this) {
            if (cacheFile.exists()) {
                log.info("Clearing cache and deleting cache file (recreation is forced): " + cacheFile.getAbsolutePath());
                cacheFile.delete();
            }
            clear();
            state = STATE.SAVED;
        }
    }

    protected void setDirty() {
        state = STATE.DIRTY;
    }

    public void read() {
        try {
            if (!cacheFile.exists()) {
                // Cache file doesn't exist. Nothing to read.
                state = STATE.DIRTY; // Needed to save cache to file.
                return;
            }
            log.info("Parsing cache file '" + cacheFile.getAbsolutePath() + "'.");
            String json;
            if (compress) {
                try (GzipCompressorInputStream in = new GzipCompressorInputStream(new FileInputStream(cacheFile))) {
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(in, writer, Definitions.STD_CHARSET);
                    json = writer.toString();
                }
            } else {
                json = FileUtils.readFileToString(cacheFile, Definitions.STD_CHARSET);
            }
            AbstractCache readCache = JsonUtils.fromJson(this.getClass(), json);
            if (readCache != null) {
                this.lastModified = readCache.lastModified;
                this.created = readCache.created;
                this.state = STATE.SAVED; // State of cache is updated from cache file.
                update(readCache);
            } else {
                log.error("Error while parsing cache: " + cacheFile.getAbsolutePath());
                this.state = STATE.DIRTY; // Needed to save cache to file.
            }
        } catch (IOException ex) {
            log.error("Error while trying to read cache file '" + cacheFile.getAbsolutePath() + "': "
                    + ex.getMessage(), ex);
            this.state = STATE.DIRTY; // Needed to save cache to file.
        }
    }

    protected abstract void update(AbstractCache readCache);

    public void save() {
        if (this.state == STATE.SAVED || this.state == STATE.INITIAL) {
            log.info("Cache file is up to date (nothing to save): " + cacheFile);
            return;
        }
        log.info("Saving to cache file: " + cacheFile);
        if (created == null) {
            created = lastModified = new Date();
        } else {
            lastModified = new Date();
        }
        String json = JsonUtils.toJson(this);
        try {
            if (this.compress) {
                try (GzipCompressorOutputStream out = new GzipCompressorOutputStream(new FileOutputStream(cacheFile))) {
                    IOUtils.copy(new StringReader(json), out, Definitions.STD_CHARSET);
                }
            } else {
                FileUtils.write(cacheFile, json, Definitions.STD_CHARSET);
            }
            this.state = STATE.SAVED;
        } catch (IOException ex) {
            log.error("Error while trying to write cache file '" + cacheFile.getAbsolutePath() + "': "
                    + ex.getMessage(), ex);
            this.state = STATE.DIRTY;
        }
    }

    /**
     * Needed by jackson for deserialization.
     */
    AbstractCache() {
    }

    AbstractCache(File cacheDir, String cacheFilename) {
        this(cacheDir, cacheFilename, false);
    }

    AbstractCache(File cacheDir, String cacheFilename, boolean zip) {
        this.compress = zip;
        String filename = CACHE_FILE_PREFIX + cacheFilename + "." + CACHE_FILE_EXTENSION;
        if (this.compress)
            filename = filename + CACHE_FILE_ZIP_EXTENSION;
        cacheFile = new File(cacheDir, filename);
        this.state = STATE.INITIAL;
    }

    public static boolean isCacheFile(File file) {
        String filename = file.getName();
        String extension = FilenameUtils.getExtension(filename);
        return filename.startsWith(CACHE_FILE_PREFIX) &&
                (extension.equals(CACHE_FILE_EXTENSION)
                        || extension.equals(CACHE_FILE_EXTENSION + CACHE_FILE_ZIP_EXTENSION));
    }
}
