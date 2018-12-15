package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;

public class BorgArchiveLimits implements Serializable {
    private static final long serialVersionUID = -3079958893130481516L;
    @JsonProperty("max_archive_size")
    @Getter
    private double maxArchiveSize;
}
