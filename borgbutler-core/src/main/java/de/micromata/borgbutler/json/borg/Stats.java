package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;

public class Stats implements Serializable {
    private static final long serialVersionUID = 9141985857856734073L;
    @Getter
    @JsonProperty("total_chunks")
    private long totalChunks;
    @Getter
    @JsonProperty("total_csize")
    private long totalCSize;
    @Getter
    @JsonProperty("total_size")
    private long totalSize;
    @Getter
    @JsonProperty("total_unique_chunks")
    private long totalUniqueChunks;
    @Getter
    @JsonProperty("unique_csize")
    private long uniqueCSize;
    @Getter
    @JsonProperty("unique_size")
    private long uniqueSize;
}
