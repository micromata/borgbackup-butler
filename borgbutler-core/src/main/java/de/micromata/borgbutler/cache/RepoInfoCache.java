package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.json.borg.RepoInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class RepoInfoCache extends AbstractCache<RepoInfo> {
    private static Logger log = LoggerFactory.getLogger(RepoInfoCache.class);
    public static final String CACHE_REPOS_BASENAME = "repos";

    /**
     * Needed by jackson for deserialization.
     */
    RepoInfoCache() {
    }

    RepoInfoCache(File cacheDir) {
        super(cacheDir, CACHE_REPOS_BASENAME);
    }
}
