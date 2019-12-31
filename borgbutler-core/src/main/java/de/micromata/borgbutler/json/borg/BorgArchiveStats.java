package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class BorgArchiveStats implements Serializable {
    private static final long serialVersionUID = -7603297185652222010L;
    @JsonProperty("compressed_size")
    private long compressedSize;
    @JsonProperty("deduplicated_size")
    private long deduplicatedSize;
    private long nfiles;
    @JsonProperty("original_size")
    private long originalSize;

    public long getCompressedSize() {
        return this.compressedSize;
    }

    public long getDeduplicatedSize() {
        return this.deduplicatedSize;
    }

    public long getNfiles() {
        return this.nfiles;
    }

    public long getOriginalSize() {
        return this.originalSize;
    }
}
