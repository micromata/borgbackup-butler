package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public class Repository {
    @Getter
    private String id;
    @Getter
    @JsonProperty("last_modified")
    private String lastModified;
    @Getter
    private String location;
}
