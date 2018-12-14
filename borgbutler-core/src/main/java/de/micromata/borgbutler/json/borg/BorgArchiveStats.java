package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;

public class BorgArchiveStats implements Serializable {
    private static final long serialVersionUID = -7603297185652222010L;
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
