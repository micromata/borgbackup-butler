package de.micromata.borgbutler.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Configuration {
    @Getter
    private String borgCommand = "borg";
    /**
     * Default is 100 MB (approximately).
     */
    @Getter
    @JsonProperty("cache_archive_content_max_disc_size_mb")
    private int cacheArchiveContentMaxDiscSizeMB = 100;

    @Getter
    @JsonProperty("repo-configs")
    private List<BorgRepoConfig> repoConfigs = new ArrayList<>();

    public void add(BorgRepoConfig repoConfig) {
        repoConfigs.add(repoConfig);
    }

    public BorgRepoConfig getRepoConfig(String idOrName) {
        if (idOrName == null) {
            return null;
        }
        for (BorgRepoConfig repoConfig : repoConfigs) {
            if (StringUtils.equals(idOrName, repoConfig.getRepo()) || StringUtils.equals(idOrName, repoConfig.getId())) {
                return repoConfig;
            }
        }
        return null;
    }
}
