package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.json.borg.RepoInfo;
import de.micromata.borgbutler.json.borg.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class RepoInfoCache extends AbstractCache<RepoInfo> {
    private static Logger log = LoggerFactory.getLogger(RepoInfoCache.class);
    public static final String CACHE_REPOS_FILENAME = "cache-repos.json";

    public boolean matches(RepoInfo element, String identifier) {
        Repository repository = element.getRepository();
        if (repository == null) {
            return false;
        }
        return identifier.equals(repository.getId()) || identifier.equals(repository.getName())
                || identifier.equals(repository.getLocation());
    }

    public String getIdentifier(RepoInfo element) {
        return element.getRepository().getId();
    }

    public void updateFrom(RepoInfo dest, RepoInfo source) {
        dest.updateFrom(source);
    }

    /**
     * Needed by jackson for deserialization.
     */
    RepoInfoCache() {
    }

    RepoInfoCache(File cacheDir) {
        super(cacheDir, CACHE_REPOS_FILENAME);
    }
}
