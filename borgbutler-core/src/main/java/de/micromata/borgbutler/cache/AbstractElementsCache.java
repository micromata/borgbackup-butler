package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.config.BorgRepoConfig;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractElementsCache<T> extends AbstractCache {
    private static Logger log = LoggerFactory.getLogger(AbstractElementsCache.class);

    @Getter
    protected Map<String, T> elements = new HashMap<>();

    public T get(BorgRepoConfig repoConfig, String identifier) {
        if (identifier == null) {
            return null;
        }
        if (getState() == STATE.INITIAL) {
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
        super.clear();
    }

    public void upsert(BorgRepoConfig repoConfig, T element) {
        T existingElement = get(repoConfig, getIdentifier(element));
        if (existingElement == null) {
            elements.put(getIdentifier(element), element);
        } else {
            updateFrom(existingElement, element);
        }
        setDirty();
    }

    protected void update(AbstractCache readCache) {
        this.elements = ((AbstractElementsCache)readCache).elements;
    }

    /**
     * Needed by jackson for deserialization.
     */
    AbstractElementsCache() {
    }

    AbstractElementsCache(File cacheDir, String cacheFilename) {
        super(cacheDir, cacheFilename);
    }

    AbstractElementsCache(File cacheDir, String cacheFilename, boolean compress) {
        super(cacheDir, cacheFilename, compress);
    }
}
