package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.BorgCommands;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.Configuration;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.json.borg.Archive1;
import de.micromata.borgbutler.json.borg.ArchiveList;
import de.micromata.borgbutler.json.borg.RepoInfo;
import de.micromata.borgbutler.json.borg.RepoList;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class CacheTest {
    private static Logger log = LoggerFactory.getLogger(CacheTest.class);

    @Test
    void repoCacheTest() {
        ConfigurationHandler configHandler = ConfigurationHandler.getInstance();
        configHandler.read();
        Configuration config = ConfigurationHandler.getConfiguration();
        if (config.getRepoConfigs().size() == 0) {
            log.info("No repos configured. Please configure repos first in: " + configHandler.getConfigFile().getAbsolutePath());
            return;
        }
        ButlerCache butlerCache = ButlerCache.getInstance();
        //butlerCache.removeAllCacheFiles();
        butlerCache.read();
        {
            RepoInfoCache repoInfoCache = ButlerCache.getRepoInfoCache();
            if (repoInfoCache.getElements().size() != config.getRepoConfigs().size()) {
                refreshRepoInfoCache(config, repoInfoCache);
            }
            assertEquals(config.getRepoConfigs().size(), repoInfoCache.getElements().size());
        }
        {
            RepoListCache repoListCache = ButlerCache.getRepoListCache();
            if (repoListCache.getElements().size() != config.getRepoConfigs().size()) {
                refreshRepoListCache(config, repoListCache);
            }
            assertEquals(config.getRepoConfigs().size(), repoListCache.getElements().size());
        }
        {
            List<BorgRepoConfig> repoConfigs = ConfigurationHandler.getConfiguration().getRepoConfigs();
            if (CollectionUtils.isNotEmpty(repoConfigs)) {
                BorgRepoConfig repoConfig = repoConfigs.get(0);
                RepoList repoList = ButlerCache.getRepoListCache().get(repoConfig.getRepo());
                if (repoList != null && CollectionUtils.isNotEmpty(repoList.getArchives())) {
                    Archive1 archive = repoList.getArchives().get(0);
                    if (archive != null) {
                        ArchiveList list = BorgCommands.info(repoConfig, archive);
                        log.info(list.toString());
                    }
                }
            }
        }
        butlerCache.save();
    }

    private void refreshRepoInfoCache(Configuration config, RepoInfoCache repoInfoCache) {
        for (BorgRepoConfig repo : config.getRepoConfigs()) {
            log.info("Processing repo info '" + repo + "'");
            RepoInfo repoInfo = BorgCommands.info(repo);
            repoInfoCache.upsert(repoInfo);
            repoInfo = repoInfoCache.get(repoInfo.getRepository().getId());
            assertNotNull(repoInfo);
        }
    }

    private void refreshRepoListCache(Configuration config, RepoListCache repoListCache) {
        for (BorgRepoConfig repo : config.getRepoConfigs()) {
            log.info("Processing repo list '" + repo + "'");
            RepoList repoList = BorgCommands.list(repo);
            repoListCache.upsert(repoList);
            repoList = repoListCache.get(repoList.getRepository().getId());
            assertNotNull(repoList);
        }
    }

    private void refresArchiveListCache(Configuration config, RepoListCache repoListCache) {
        for (BorgRepoConfig repo : config.getRepoConfigs()) {
            log.info("Processing repo list '" + repo + "'");
            RepoList repoList = BorgCommands.list(repo);
            repoListCache.upsert(repoList);
            repoList = repoListCache.get(repoList.getRepository().getId());
            assertNotNull(repoList);
        }
    }
}
