package de.micromata.borgbutler.config;

import lombok.Getter;
import lombok.Setter;

public class BorgRepo {
    @Getter @Setter
    private String name;
    @Getter @Setter
    private String repo;
    @Getter @Setter
    private String rsh;
    @Getter @Setter
    private String password;
    @Getter @Setter
    private String passwordCommand;
}
