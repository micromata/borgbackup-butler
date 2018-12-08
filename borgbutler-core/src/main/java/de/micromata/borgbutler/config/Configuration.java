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
}
