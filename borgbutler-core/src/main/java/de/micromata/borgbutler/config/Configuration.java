package de.micromata.borgbutler.config;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Configuration {
    @Getter
    private List<BorgRepo> repos = new ArrayList<>();

    public void add(BorgRepo repo) {
        repos.add(repo);
    }
}
