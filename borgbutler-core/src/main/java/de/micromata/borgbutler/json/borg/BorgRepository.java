package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Part of Borg json objects to refer objects to repositories.
 */
public class BorgRepository implements Serializable {
    private static final long serialVersionUID = 1278802519434516280L;
    private String id;
    @JsonProperty("last_modified")
    private String lastModified;
    private String location;

    public String getId() {
        return this.id;
    }

    public String getLastModified() {
        return this.lastModified;
    }

    public String getLocation() {
        return this.location;
    }
}
