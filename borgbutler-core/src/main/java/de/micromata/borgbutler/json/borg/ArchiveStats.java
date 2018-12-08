package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public class ArchiveStats {
    @Getter
    @JsonProperty("compressed_size")
    private long compressedSize;
    @Getter
    @JsonProperty("deduplicated_size")
    private long deduplicatedSize;
    @Getter
    private long nfiles;
    @Getter
    @JsonProperty("original_size")
    private long originalSize;
}
