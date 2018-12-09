package de.micromata.borgbutler.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.micromata.borgbutler.config.BorgRepoConfig;
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
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCache<T> {
    private static Logger log = LoggerFactory.getLogger(AbstractCache.class);
    private static final String CACHE_FILE_PREFIX = "cache-";
    private static final String CACHE_FILE_EXTENSION = "json";
    private static final String CACHE_FILE_ZIP_EXTENSION = ".zip";

    /**
     * INITIAL - on startup (not yet read), DIRTY - modifications not written, SAVED - content written to file.
     */
    private enum STATE {INITIAL, DIRTY, SAVED}

    @JsonIgnore
    protected File cacheFile;
    @JsonIgnore
    @Getter
    private boolean zip;
    @JsonIgnore
    private STATE state = STATE.INITIAL;
    @Getter
    private Date lastModified;
    @Getter
    private Date created;
    @Getter
    protected Map<String, T> elements = new HashMap<>();

    public T get(BorgRepoConfig repoConfig, String identifier) {
        if (identifier == null) {
            return null;
        }
        if (this.state == STATE.INITIAL) {
            read();
        }
        for (T element : elements.values()) {
            if (matches(element, identifier)) {
                return element;
            }
        }
        return load(repoConfig, identifier);
    }

    protected abstract T load(BorgRepoConfig repoConfig, String identifier);

    public abstract boolean matches(T element, String identifier);

    public abstract String getIdentifier(T element);

    public abstract void updateFrom(T dest, T source);

    /**
     * Removes all entries (doesn't effect the cache files!).
     */
    public void clear() {
        elements.clear();
        state = STATE.DIRTY;
    }

    public void upsert(BorgRepoConfig repoConfig, T element) {
        T existingElement = get(repoConfig, getIdentifier(element));
        if (existingElement == null) {
            elements.put(getIdentifier(element), element);
        } else {
            updateFrom(existingElement, element);
        }
        state = STATE.DIRTY; // Needed to save cache to file.
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
            if (zip) {
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
                this.elements = readCache.elements;
                this.lastModified = readCache.lastModified;
                this.created = readCache.created;
                this.state = STATE.SAVED; // State of cache is updated from cache file.
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
            if (this.zip) {
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
        this.zip = zip;
        String filename = CACHE_FILE_PREFIX + cacheFilename + "." + CACHE_FILE_EXTENSION;
        if (this.zip)
            filename = filename + CACHE_FILE_EXTENSION;
        cacheFile = new File(cacheDir, CACHE_FILE_PREFIX + cacheFilename + "." + CACHE_FILE_EXTENSION);
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
