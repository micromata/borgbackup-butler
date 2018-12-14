package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;

/**
 * Part of Borg json objects to refer objects to repositories.
 */
public class BorgRepository implements Serializable {
    private static final long serialVersionUID = 1278802519434516280L;
    @Getter
    private String id;
    @Getter
    @JsonProperty("last_modified")
    private String lastModified;
    @Getter
    private String location;
}
