package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.micromata.borgbutler.json.JsonUtils;
import lombok.Getter;

/**
 * This object is given by <tt>borg list archive</tt>.
 */
public class Archive2 {
    @Getter
    @JsonProperty("chunker_params")
    private int[] chunkerParams;
    /**
     * The command line used for creating this archive: borg create --filter...
     */
    @Getter
    @JsonProperty("command_line")
    private String[] commandLine;
    @Getter
    private String comment;
    @Getter
    private String start;
    @Getter
    private ArchiveStats stats;
    @Getter
    private String username;
    public String toString() {
        return JsonUtils.toJson(this, true);
    }
}
