package de.micromata.borgbutler.config;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Configuration {
    @Getter
    private String borgCommand = "borg";
    @Getter
    private List<BorgRepoConfig> repos = new ArrayList<>();

    public void add(BorgRepoConfig repo) {
        repos.add(repo);
    }

    public BorgRepoConfig getRepo(String idOrName) {
        if (idOrName == null) {
            return null;
        }
        for (BorgRepoConfig repoConfig : repos) {
            if (idOrName.equals(repoConfig.getRepo()) ||idOrName.equals(repoConfig.getName())) {
                return repoConfig;
            }
        }
        return null;
    }
}
