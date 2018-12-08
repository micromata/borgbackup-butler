package de.micromata.borgbutler.json.borg;

import lombok.Getter;

public class Cache {
    @Getter
    private String path;
    @Getter
    private Stats stats;
}
