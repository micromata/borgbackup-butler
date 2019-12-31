package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class BorgStats implements Serializable {
    private static final long serialVersionUID = 9141985857856734073L;
    @JsonProperty("total_chunks")
    private long totalChunks;
    @JsonProperty("total_csize")
    private long totalCSize;
    @JsonProperty("total_size")
    private long totalSize;
    @JsonProperty("total_unique_chunks")
    private long totalUniqueChunks;
    @JsonProperty("unique_csize")
    private long uniqueCSize;
    @JsonProperty("unique_size")
    private long uniqueSize;

    public long getTotalChunks() {
        return this.totalChunks;
    }

    public long getTotalCSize() {
        return this.totalCSize;
    }

    public long getTotalSize() {
        return this.totalSize;
    }

    public long getTotalUniqueChunks() {
        return this.totalUniqueChunks;
    }

    public long getUniqueCSize() {
        return this.uniqueCSize;
    }

    public long getUniqueSize() {
        return this.uniqueSize;
    }
}
