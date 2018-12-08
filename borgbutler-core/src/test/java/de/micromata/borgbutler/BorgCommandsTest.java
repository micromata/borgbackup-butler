package de.micromata.borgbutler;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.Configuration;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.json.borg.RepoInfo;
import de.micromata.borgbutler.json.borg.RepoList;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BorgCommandsTest {
    private static Logger log = LoggerFactory.getLogger(BorgCommandsTest.class);
    @Test
    void infoTest() {
        ConfigurationHandler configHandler = ConfigurationHandler.getInstance();
        configHandler.read();
        Configuration config = ConfigurationHandler.getConfiguration();
        if (config.getRepos().size() == 0) {
            log.info("No repos configured. Please configure repos first in: " + configHandler.getConfigFile().getAbsolutePath());
            return;
        }
        for (BorgRepoConfig repo : config.getRepos()) {
            log.info("Processing repo '" + repo + "'");
            RepoInfo repoInfo = BorgCommands.info(repo);
            log.info("Repo info: " + repoInfo);
            RepoList repoList = BorgCommands.list(repo);
            log.info("Repo list: " + repoList);
        }
    }
}
