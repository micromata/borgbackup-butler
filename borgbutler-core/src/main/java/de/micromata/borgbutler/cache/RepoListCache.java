package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.json.borg.RepoList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class RepoListCache extends AbstractCache<RepoList> {
    private static Logger log = LoggerFactory.getLogger(RepoListCache.class);
    public static final String CACHE_REPO_LISTS_BASENAME = "repo-lists";

    /**
     * Needed by jackson for deserialization.
     */
    RepoListCache() {
    }

    RepoListCache(File cacheDir) {
        super(cacheDir, CACHE_REPO_LISTS_BASENAME);
    }
}
