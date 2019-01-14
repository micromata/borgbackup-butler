package de.micromata.borgbutler.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.micromata.borgbutler.demo.DemoRepos;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
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

    @Getter
    private String[][] binaries = {
            {"freebsd64", "FreeBSD 64"},
            {"linux32", "Linux 32"},
            {"linux64", "Linux 64"},
            {"macosx64", "MacOS X 64"}};

    @JsonIgnore
    @Setter(AccessLevel.PACKAGE)
    private File workingDir;

    @Getter
    private String borgCommand = "borg";
    /**
     * Default is 100 MB (approximately).
     */
    @Getter
    private int maxArchiveContentCacheCapacityMb = 100;

    @Getter
    @Setter
    private boolean showDemoRepos = true;

    /**
     * Default is restore inside BorgButler's home dir (~/.borgbutler/restore).
     */
    @Getter
    @JsonProperty("restoreDir")
    private String restoreDirPath;
    @JsonIgnore
    private File restoreHomeDir;

    private List<BorgRepoConfig> repoConfigs = new ArrayList<>();

    public void add(BorgRepoConfig repoConfig) {
        repoConfigs.add(repoConfig);
    }

    public BorgRepoConfig getRepoConfig(String idOrName) {
        if (idOrName == null) {
            return null;
        }
        for (BorgRepoConfig repoConfig : getRepoConfigs()) {
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

    public List<BorgRepoConfig> getRepoConfigs() {
        if (!ConfigurationHandler.getConfiguration().isShowDemoRepos()) {
            return repoConfigs;
        }
        List<BorgRepoConfig> result = new ArrayList<>();
        result.addAll(repoConfigs);
        DemoRepos.addDemoRepos(result);
        return result;
    }

    List<BorgRepoConfig> _getRepoConfigs() {
        return repoConfigs;
    }
}
