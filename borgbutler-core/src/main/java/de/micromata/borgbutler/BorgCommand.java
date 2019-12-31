package de.micromata.borgbutler;

import de.micromata.borgbutler.config.BorgRepoConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Represents a command to be queued for executing.
 */
public class BorgCommand {
    private Logger log = LoggerFactory.getLogger(BorgCommand.class);

    private File workingDir;
    private String[] args;
    private String[] params;
    private BorgRepoConfig repoConfig;
    private String command;
    private String archive;
    /**
     * For displaying and information purposes for the user only, when browsing the current command queue.
     */
    private String description;
    /**
     * The result of the call will be written to this String.
     */
    private String response;

    BorgCommand setArgs(String... args) {
        this.args = args;
        return this;
    }

    BorgCommand setParams(String... params) {
        this.params = params;
        return this;
    }

    public String getRepoArchive() {
        if (archive == null) {
            if (repoConfig == null) {
                return null;
            }
            return repoConfig.getRepo();
        }
        return repoConfig.getRepo() + "::" + archive;
    }

    /**
     *
     * @return Abbreviated response e. g. for logging an error.
     */
    public String getAbbreviatedResponse() {
        return StringUtils.abbreviate(response, 1000);
    }

    public File getWorkingDir() {
        return this.workingDir;
    }

    public String[] getArgs() {
        return this.args;
    }

    public String[] getParams() {
        return this.params;
    }

    public BorgRepoConfig getRepoConfig() {
        return this.repoConfig;
    }

    public String getCommand() {
        return this.command;
    }

    public String getArchive() {
        return this.archive;
    }

    public String getDescription() {
        return this.description;
    }

    public String getResponse() {
        return this.response;
    }

    public BorgCommand setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
        return this;
    }

    public BorgCommand setRepoConfig(BorgRepoConfig repoConfig) {
        this.repoConfig = repoConfig;
        return this;
    }

    public BorgCommand setCommand(String command) {
        this.command = command;
        return this;
    }

    public BorgCommand setArchive(String archive) {
        this.archive = archive;
        return this;
    }

    public BorgCommand setDescription(String description) {
        this.description = description;
        return this;
    }

    BorgCommand setResponse(String response) {
        this.response = response;
        return this;
    }
}
