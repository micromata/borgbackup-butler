package de.micromata.borgbutler.config;

import lombok.Getter;

import java.util.List;

public class Configuration {
    @Getter
    private List<BorgRepo> repos;

    public void add(BorgRepo repo) {
        repos.add(repo);
    }
}
