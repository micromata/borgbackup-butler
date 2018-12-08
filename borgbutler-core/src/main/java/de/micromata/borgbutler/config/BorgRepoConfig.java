package de.micromata.borgbutler.config;

import lombok.Getter;
import lombok.Setter;

public class BorgRepoConfig {
    /**
     * A name describing this config. Only used for displaying purposes.
     */
    @Getter @Setter
    private String name;
    @Getter @Setter
    private String repo;
    @Getter @Setter
    private String rsh;
    @Getter @Setter
    private String passphrase;
    @Getter @Setter
    private String passwordCommand;
}
