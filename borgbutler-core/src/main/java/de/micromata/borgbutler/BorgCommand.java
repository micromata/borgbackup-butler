package de.micromata.borgbutler;

import de.micromata.borgbutler.config.BorgRepoConfig;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import java.io.File;

public class BorgCommand {
    private Logger log = LoggerFactory.getLogger(BorgCommand.class);

    @Setter
    File workingDir;
    String[] args;
    String[] params;
    @Setter
    BorgRepoConfig repoConfig;
    @Setter
    String command;
    @Setter
    String archive;

    BorgCommand setArgs(String... args) {
        this.args = args;
        return this;
    }

    BorgCommand setParams(String... params) {
        this.params = params;
        return this;
    }

    String getRepoArchive() {
        if (archive == null) {
            return repoConfig.getRepo();
        }
        return repoConfig.getRepo() + "::" + archive;
    }
}
