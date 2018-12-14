package de.micromata.borgbutler.data;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.json.borg.BorgCache;
import de.micromata.borgbutler.json.borg.BorgEncryption;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Part of Borg json objects to refer objects to repositories.
 */
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
    @Setter
    private String id;
    /**
     * UTC date.
     */
    @Getter
    @Setter
    private String lastModified;
    @Getter
    @Setter
    private String location;

    @Getter
    @Setter
    private String securityDir;
    @Getter
    @Setter
    private BorgCache cache;
    @Getter
    @Setter
    private BorgEncryption encryption;

    public Repository() {
    }
}
