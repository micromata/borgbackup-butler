package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.ConfigurationHandler;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class Repository implements Serializable {
    private static final long serialVersionUID = 1278802519434516280L;
    /**
     * A name describing this config. Only used for displaying purposes. This is automatically set with the name
     * of the repository configuration.
     *
     * @see BorgRepoConfig#getName()
     */
    @Getter
    @Setter
    String name;
    @Getter
    private String id;
    @Getter
    @JsonProperty("last_modified")
    private String lastModified;
    @Getter
    private String location;

    /**
     * Sets also the name for this repository if available in the configuration.
     *
     * @param location
     * @return
     */
    public Repository setLocation(String location) {
        this.location = location;
        // It's ugly but efficiently ;-)
        BorgRepoConfig repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(location);
        if (repoConfig != null) {
            this.name = repoConfig.getName();
        }
        return this;
    }
}
