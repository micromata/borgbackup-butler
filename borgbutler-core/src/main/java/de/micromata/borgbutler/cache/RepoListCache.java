package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.json.borg.RepoList;
import de.micromata.borgbutler.json.borg.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class RepoListCache extends AbstractCache<RepoList> {
    private static Logger log = LoggerFactory.getLogger(RepoListCache.class);
    public static final String CACHE_REPO_LISTS_BASENAME = "repo-lists";

    public boolean matches(RepoList element, String identifier) {
        Repository repository = element.getRepository();
        if (repository == null) {
            return false;
        }
        return identifier.equals(repository.getId()) || identifier.equals(repository.getName())
                || identifier.equals(repository.getLocation());
    }

    public String getIdentifier(RepoList element) {
        return element.getRepository().getId();
    }

    public void updateFrom(RepoList dest, RepoList source) {
        dest.updateFrom(source);
    }

    /**
     * Needed by jackson for deserialization.
     */
    RepoListCache() {
    }

    RepoListCache(File cacheDir) {
        super(cacheDir, CACHE_REPO_LISTS_BASENAME);
    }
}
