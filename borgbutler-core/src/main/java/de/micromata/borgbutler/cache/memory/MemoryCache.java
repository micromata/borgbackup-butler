package de.micromata.borgbutler.cache.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MemoryCache<I, M extends MemoryCacheObject<I>> {
    private static Logger log = LoggerFactory.getLogger(MemoryCache.class);
    private long maxMemorySize;
    private List<M> recents = new ArrayList<>();

    public MemoryCache(long maxMemorySize) {
        this.maxMemorySize = maxMemorySize;
    }

    public M getRecent(I identifier) {
        synchronized (recents) {
            for (M entry : recents) {
                if (entry._matches(identifier)) {
                    log.debug("Getting recent entry: " + entry.getIdentifier());
                    return entry;
                }
            }
        }
        return null;
    }

    public void add(M newEntry) {
        if (newEntry.getSize() > maxMemorySize) {
            // Object to large for storing in memory.
            return;
        }
        synchronized (recents) {
            recents.add(newEntry);
            log.debug("Add new recent entry: " + newEntry.getIdentifier());
            int size;
            while (true) {
                size = 0;
                MemoryCacheObject<I> oldest = null;
                for (MemoryCacheObject<I> entry : recents) {
                    if (oldest == null || entry.lastAcess < oldest.lastAcess) {
                        oldest = entry;
                    }
                    size += entry.getSize();
                }
                if (oldest == null) {
                    break;
                }
                if (size >= maxMemorySize) {
                    log.debug("Removing oldest recent entry: " + oldest.getIdentifier());
                    recents.remove(oldest);
                } else {
                    break;
                }
            }
        }
    }
}
