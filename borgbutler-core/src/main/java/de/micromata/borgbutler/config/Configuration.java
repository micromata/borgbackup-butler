package de.micromata.borgbutler.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.micromata.borgbutler.demo.DemoRepos;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Configuration {
    private Logger log = LoggerFactory.getLogger(Configuration.class);
    /**
     * Default dir name for restoring archives.
     */
    private static final String RESTORE_DIRNAME = "restore";

    @JsonIgnore
    private File workingDir;
    /**
     * The path of the borg command to use.
     */
    private String borgCommand;

    /**
     * Default is 100 MB (approximately).
     */
    private int maxArchiveContentCacheCapacityMb = 100;

    private boolean showDemoRepos = true;

    /**
     * Default is restore inside BorgButler's home dir (~/.borgbutler/restore).
     */
    @JsonProperty("restoreDir")
    private String restoreDirPath;
    @JsonIgnore
    private File restoreHomeDir;

    @JsonProperty
    private List<BorgRepoConfig> repoConfigs = new ArrayList<>();

    public void add(BorgRepoConfig repoConfig) {
        synchronized (repoConfigs) {
            repoConfigs.add(repoConfig);
        }
    }

    public boolean remove(String idOrName) {
        if (idOrName == null) {
            return false;
        }
        synchronized (repoConfigs) {
            for (BorgRepoConfig repoConfig : getAllRepoConfigs()) {
                if (StringUtils.equals(idOrName, repoConfig.getRepo()) || StringUtils.equals(idOrName, repoConfig.getId())) {
                    repoConfigs.remove(repoConfig);
                    return true;
                }
            }
        }
        return false;
    }

    public BorgRepoConfig getRepoConfig(String idOrName) {
        if (idOrName == null) {
            return null;
        }
        for (BorgRepoConfig repoConfig : getAllRepoConfigs()) {
            if (StringUtils.equals(idOrName, repoConfig.getRepo()) || StringUtils.equals(idOrName, repoConfig.getId())) {
                return repoConfig;
            }
        }
        return null;
    }

    public File getRestoreHomeDir() {
        if (restoreHomeDir == null) {
            if (StringUtils.isNotBlank(restoreDirPath)) {
                restoreHomeDir = new File(restoreDirPath);
            } else {
                restoreHomeDir = new File(workingDir, RESTORE_DIRNAME);
            }
            if (!restoreHomeDir.exists()) {
                log.info("Creating dir '" + restoreHomeDir.getAbsolutePath() + "' for restoring backup files and directories.");
            }
        }
        return restoreHomeDir;
    }

    public void copyFrom(Configuration other) {
        this.borgCommand = other.borgCommand;
        this.maxArchiveContentCacheCapacityMb = other.maxArchiveContentCacheCapacityMb;
        this.showDemoRepos = other.showDemoRepos;
    }

    @JsonIgnore
    public List<BorgRepoConfig> getAllRepoConfigs() {
        return DemoRepos.getAllRepos(repoConfigs);
    }

    List<BorgRepoConfig> getRepoConfigs() {
        return repoConfigs;
    }

    public String getBorgCommand() {
        return this.borgCommand;
    }

    public int getMaxArchiveContentCacheCapacityMb() {
        return this.maxArchiveContentCacheCapacityMb;
    }

    public boolean isShowDemoRepos() {
        return this.showDemoRepos;
    }

    public String getRestoreDirPath() {
        return this.restoreDirPath;
    }

    Configuration setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
        return this;
    }

    public Configuration setBorgCommand(String borgCommand) {
        this.borgCommand = borgCommand;
        return this;
    }

    public Configuration setShowDemoRepos(boolean showDemoRepos) {
        this.showDemoRepos = showDemoRepos;
        return this;
    }
}
