package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.Configuration;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.data.Archive;
import de.micromata.borgbutler.data.Repository;
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
        Configuration config = ConfigurationHandler.getConfiguration();
        if (config.getRepoConfigs().size() == 0) {
            log.info("No repos configured. Please configure repos first in: " + configHandler.getConfigFile().getAbsolutePath());
            return;
        }
        ButlerCache butlerCache = ButlerCache.getInstance();
        {
            for (BorgRepoConfig repoConfig : ConfigurationHandler.getConfiguration().getRepoConfigs()) {
                Repository repository = ButlerCache.getInstance().getRepository(repoConfig.getRepo());
            }
            assertEquals(config.getRepoConfigs().size(), ButlerCache.getInstance().getAllRepositories().size());
        }
        {
            for (BorgRepoConfig repoConfig : ConfigurationHandler.getConfiguration().getRepoConfigs()) {
                Repository repository = ButlerCache.getInstance().getRepositoryArchives(repoConfig.getRepo());
            }
            assertEquals(config.getRepoConfigs().size(), ButlerCache.getInstance().getAllRepositories().size());
        }
        List<BorgRepoConfig> repoConfigs = ConfigurationHandler.getConfiguration().getRepoConfigs();
        Archive archive = null;
        BorgRepoConfig repoConfig = null;
        if (CollectionUtils.isNotEmpty(repoConfigs)) {
            repoConfig = repoConfigs.get(0);
            Repository rerepositoryoList = ButlerCache.getInstance().getRepositoryArchives(repoConfig.getRepo());
            if (rerepositoryoList != null && CollectionUtils.isNotEmpty(rerepositoryoList.getArchives())) {
                archive = rerepositoryoList.getArchives().first();
            }
        }
        {
            if (archive != null) {
                Archive archive2 = ButlerCache.getInstance().getArchive(repoConfig.getRepo(), archive.getName());
                assertNotNull(archive2);
                archive = ButlerCache.getInstance().getArchive(repoConfig.getRepo(), archive.getId());
                assertNotNull(archive2);
                List<FilesystemItem> content = ButlerCache.getInstance().getArchiveContent(repoConfig, archive2);
                log.info("Number of items (content) of archive: " + content.size());
                content = ButlerCache.getInstance().getArchiveContent(repoConfig, archive2);
                log.info("Number of items (content) of archive: " + content.size());
            }
        }
        ButlerCache.getInstance().shutdown();
    }
}
