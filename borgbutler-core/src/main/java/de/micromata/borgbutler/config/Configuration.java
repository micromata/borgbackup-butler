package de.micromata.borgbutler.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Configuration {
    @Getter
    private String borgCommand = "borg";
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
            if (idOrName.equals(repoConfig.getRepo()) ||idOrName.equals(repoConfig.getName())) {
                return repoConfig;
            }
        }
        return null;
    }
}
