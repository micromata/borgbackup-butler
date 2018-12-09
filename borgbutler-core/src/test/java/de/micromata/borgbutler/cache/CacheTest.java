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
        //butlerCache.removeAllCacheFiles();
        {
            for (BorgRepoConfig repoConfig : ConfigurationHandler.getConfiguration().getRepoConfigs()) {
                RepoInfo repoInfo = ButlerCache.getInstance().getRepoInfoCache().get(repoConfig, repoConfig.getRepo());
            }
            assertEquals(config.getRepoConfigs().size(), ButlerCache.getInstance().getRepoInfoCache().getElements().size());
        }
        {
            for (BorgRepoConfig repoConfig : ConfigurationHandler.getConfiguration().getRepoConfigs()) {
                RepoList repoList = ButlerCache.getInstance().getRepoListCache().get(repoConfig, repoConfig.getRepo());
            }
            assertEquals(config.getRepoConfigs().size(), ButlerCache.getInstance().getRepoInfoCache().getElements().size());
        }
        List<BorgRepoConfig> repoConfigs = ConfigurationHandler.getConfiguration().getRepoConfigs();
        Archive archive = null;
        BorgRepoConfig repoConfig = null;
        if (CollectionUtils.isNotEmpty(repoConfigs)) {
            repoConfig = repoConfigs.get(0);
            RepoList repoList = ButlerCache.getInstance().getRepoListCache().get(repoConfig, repoConfig.getRepo());
            if (repoList != null && CollectionUtils.isNotEmpty(repoList.getArchives())) {
                archive = repoList.getArchives().get(0);
            }
        }
        {/*
            List<BorgRepoConfig> repoConfigs = ConfigurationHandler.getConfiguration().getRepoConfigs();
            if (CollectionUtils.isNotEmpty(repoConfigs)) {
                BorgRepoConfig repoConfig = repoConfigs.get(0);
                RepoList repoList = ButlerCache.getRepoListCache().get(repoConfig.getRepo());
                if (repoList != null && CollectionUtils.isNotEmpty(repoList.getArchives())) {
                    Archive1 archive = repoList.getArchives().get(0);
                    if (archive != null) {
                        ArchiveList list = ButlerCache.getArchiveListCache().get(archive.getArchive());
                        ArchiveList list = BorgCommands.info(repoConfig, archive.getArchive());
                        log.info(list.toString());
                    }
                }
            }*/
        }
        {
            if (archive != null) {
                List<FilesystemItem> content = ButlerCache.getInstance().getArchiveContent(repoConfig, archive);
                log.info("Number of items (content) of archive: " + content.size());
                content = ButlerCache.getInstance().getArchiveContent(repoConfig, archive);
            }
        }
        butlerCache.save();
    }
}
