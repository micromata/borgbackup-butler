package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.Configuration;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.json.borg.Archive;
import de.micromata.borgbutler.json.borg.FilesystemItem;
import de.micromata.borgbutler.json.borg.RepoInfo;
import de.micromata.borgbutler.json.borg.RepoList;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class CacheTest {
    private static Logger log = LoggerFactory.getLogger(CacheTest.class);

    @Test
    void repoCacheTest() {
        ConfigurationHandler configHandler = ConfigurationHandler.getInstance();
        Configuration config = ConfigurationHandler.getConfiguration();
        if (config.getRepoConfigs().size() == 0) {
            log.info("No repos configured. Please configure repos first in: " + configHandler.getConfigFile().getAbsolutePath());
            return;
        }
        ButlerCache butlerCache = ButlerCache.getInstance();
        {
            for (BorgRepoConfig repoConfig : ConfigurationHandler.getConfiguration().getRepoConfigs()) {
                RepoInfo repoInfo = ButlerCache.getInstance().getRepoInfo(repoConfig.getRepo());
            }
            assertEquals(config.getRepoConfigs().size(), ButlerCache.getInstance().getAllRepositories().size());
        }
        {
            for (BorgRepoConfig repoConfig : ConfigurationHandler.getConfiguration().getRepoConfigs()) {
                RepoList repoList = ButlerCache.getInstance().getRepoList(repoConfig.getRepo());
            }
            assertEquals(config.getRepoConfigs().size(), ButlerCache.getInstance().getAllRepositories().size());
        }
        List<BorgRepoConfig> repoConfigs = ConfigurationHandler.getConfiguration().getRepoConfigs();
        Archive archive = null;
        BorgRepoConfig repoConfig = null;
        if (CollectionUtils.isNotEmpty(repoConfigs)) {
            repoConfig = repoConfigs.get(0);
            RepoList repoList = ButlerCache.getInstance().getRepoList(repoConfig.getRepo());
            if (repoList != null && CollectionUtils.isNotEmpty(repoList.getArchives())) {
                archive = repoList.getArchives().get(0);
            }
        }
        {
            if (archive != null) {
                FilesystemItem[] content = ButlerCache.getInstance().getArchiveContent(repoConfig, archive);
                log.info("Number of items (content) of archive: " + content.length);
                content = ButlerCache.getInstance().getArchiveContent(repoConfig, archive);
                log.info("Number of items (content) of archive: " + content.length);
            }
        }
        ButlerCache.getInstance().shutdown();
    }
}
