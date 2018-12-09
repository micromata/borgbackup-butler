package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.BorgCommands;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.json.borg.RepoInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class RepoInfoCache extends AbstractElementsCache<RepoInfo> {
    private static Logger log = LoggerFactory.getLogger(RepoInfoCache.class);
    public static final String CACHE_REPOS_BASENAME = "repo-infos";

    @Override
    protected RepoInfo load(BorgRepoConfig repoConfig, String identifier) {
        RepoInfo repoInfo = BorgCommands.info(repoConfig);
        this.elements.put(getIdentifier(repoInfo), repoInfo);
        return repoInfo;
    }

    @Override
    public boolean matches(RepoInfo element, String identifier) {
        return element.matches(identifier);
    }

    @Override
    public String getIdentifier(RepoInfo element) {
        return element.getRepository().getId();
    }

    @Override
    public void updateFrom(RepoInfo dest, RepoInfo source) {
        dest.updateFrom(source);
    }

    /**
     * Needed by jackson for deserialization.
     */
    RepoInfoCache() {
    }

    RepoInfoCache(File cacheDir) {
        super(cacheDir, CACHE_REPOS_BASENAME);
    }
}
