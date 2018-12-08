package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.BorgCommands;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.json.borg.RepoList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class RepoListCache extends AbstractCache<RepoList> {
    private static Logger log = LoggerFactory.getLogger(RepoListCache.class);
    public static final String CACHE_REPO_LISTS_BASENAME = "repo-lists";

    @Override
    protected RepoList load(BorgRepoConfig repoConfig, String identifier) {
        RepoList repoList = BorgCommands.list(repoConfig);
        this.elements.put(getIdentifier(repoList), repoList);
        return repoList;
    }

    @Override
    public boolean matches(RepoList element, String identifier) {
        return element.matches(identifier);
    }

    @Override
    public String getIdentifier(RepoList element) {
        return element.getRepository().getId();
    }

    @Override
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
