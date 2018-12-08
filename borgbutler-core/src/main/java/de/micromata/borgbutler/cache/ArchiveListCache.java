package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.json.borg.ArchiveList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ArchiveListCache extends AbstractCache<ArchiveList> {
    private static Logger log = LoggerFactory.getLogger(ArchiveListCache.class);
    public static final String CACHE_ARCHIVE_LISTS_BASENAME = "archive-lists";

    /**
     * Needed by jackson for deserialization.
     */
    ArchiveListCache() {
    }

    ArchiveListCache(File cacheDir) {
        super(cacheDir, CACHE_ARCHIVE_LISTS_BASENAME);
    }
}
