package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.BorgCommands;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.Configuration;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.json.borg.RepoInfo;
import de.micromata.borgbutler.json.borg.RepoList;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class CacheTest {
    private static Logger log = LoggerFactory.getLogger(CacheTest.class);

    @Test
    void reposCacheTest() {
        ConfigurationHandler configHandler = ConfigurationHandler.getInstance();
        configHandler.read();
        Configuration config = ConfigurationHandler.getConfiguration();
        if (config.getRepos().size() == 0) {
            log.info("No repos configured. Please configure repos first in: " + configHandler.getConfigFile().getAbsolutePath());
            return;
        }
        RepoInfoCache cache = ButlerCache.getReposCache();
        cache.read();
        if (cache.getElements().size() != config.getRepos().size()) {
            refreshReposCache(config, cache);
        }
        refreshRepoListsCache(config);
        assertEquals(config.getRepos().size(), cache.getElements().size());
    }

    private void refreshReposCache(Configuration config, RepoInfoCache cache) {
        for (BorgRepoConfig repo : config.getRepos()) {
            log.info("Processing repo '" + repo + "'");
            RepoInfo repoInfo = BorgCommands.info(repo);
            cache.upsert(repoInfo);
            repoInfo = cache.get(repoInfo.getRepository().getId());
            assertNotNull(repoInfo);
        }
        cache.save();
    }

    private void refreshRepoListsCache(Configuration config) {
        for (BorgRepoConfig repo : config.getRepos()) {
            log.info("Processing repo '" + repo + "'");
            RepoList repoList = BorgCommands.list(repo);
            log.info("repoList: " + repoList);
        }
    }
}
