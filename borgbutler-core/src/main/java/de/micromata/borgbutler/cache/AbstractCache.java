package de.micromata.borgbutler.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.json.JsonUtils;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCache<T> {
    private static Logger log = LoggerFactory.getLogger(AbstractCache.class);
    private static final String CACHE_FILE_PREFIX = "cache-";
    private static final String CACHE_FILE_EXTENSION = "json";

    @JsonIgnore
    protected File cacheFile;
    @Getter
    @JsonProperty
    protected Map<String, T> elements = new HashMap<>();

    public T get(BorgRepoConfig repoConfig, String identifier) {
        if (identifier == null) {
            return null;
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
    }

    public void upsert(BorgRepoConfig repoConfig, T element) {
        T existingElement = get(repoConfig, getIdentifier(element));
        if (existingElement == null) {
            elements.put(getIdentifier(element), element);
        } else {
            updateFrom(existingElement, element);
        }
    }

    public void read() {
        try {
            if (!cacheFile.exists()) {
                // Cache file doesn't exist. Nothing to read.
                return;
            }
            log.info("Parsing cache file '" + cacheFile.getAbsolutePath() + "'.");
            String json = FileUtils.readFileToString(cacheFile, Charset.forName("UTF-8"));
            AbstractCache readCache = JsonUtils.fromJson(this.getClass(), json);
            if (readCache != null) {
                this.elements = readCache.elements;
            } else {
                log.error("Error while parsing cache: " + cacheFile.getAbsolutePath());
            }
        } catch (IOException ex) {
            log.error("Error while trying to read cache file '" + cacheFile.getAbsolutePath() + "': "
                    + ex.getMessage(), ex);
        }
    }

    public void save() {
        log.info("Saving to cache file: " + cacheFile);
        String json = JsonUtils.toJson(this);
        try {
            FileUtils.write(cacheFile, json, Charset.forName("UTF-8"));
        } catch (IOException ex) {
            log.error("Error while trying to write repos cache file '" + cacheFile.getAbsolutePath() + "': "
                    + ex.getMessage(), ex);
        }
    }

    /**
     * Needed by jackson for deserialization.
     */
    AbstractCache() {
    }

    AbstractCache(File cacheDir, String cacheFilename) {
        cacheFile = new File(cacheDir, CACHE_FILE_PREFIX + cacheFilename + "." + CACHE_FILE_EXTENSION);
    }

    public static boolean isCacheFile(File file) {
        String filename = file.getName();
        String extension = FilenameUtils.getExtension(filename);
        return filename.startsWith(CACHE_FILE_PREFIX) && extension.equals(CACHE_FILE_EXTENSION);
    }
}
