package de.micromata.borgbutler.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class BorgRepoConfig {
    /**
     * A name describing this config. Only used for displaying purposes.
     */
    @Getter @Setter
    @JsonProperty("display_name")
    private String displayName;
    @Getter @Setter
    private String repo;
    @Getter @Setter
    private String rsh;
    @Getter @Setter
    private String passphrase;
    @Getter @Setter
    private String passwordCommand;
    @Getter @Setter
    @JsonIgnore
    private String id;
}
