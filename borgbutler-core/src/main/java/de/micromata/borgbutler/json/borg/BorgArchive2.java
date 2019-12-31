package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.micromata.borgbutler.json.JsonUtils;

import java.io.Serializable;

/**
 * This object is given by <tt>borg list archive</tt>.
 */
public class BorgArchive2 implements Serializable {
    private static final long serialVersionUID = 4734056884088174992L;
    @JsonProperty("chunker_params")
    private int[] chunkerParams;
    /**
     * The command line used for creating this archive: borg create --filter...
     */
    @JsonProperty("command_line")
    private String[] commandLine;
    private String comment;
    private String start;
    private String end;
    private String duration;
    private BorgArchiveStats stats;
    private BorgArchiveLimits limits;
    private String hostname;
    private String username;

    public String toString() {
        return JsonUtils.toJson(this, true);
    }

    public int[] getChunkerParams() {
        return this.chunkerParams;
    }

    public String[] getCommandLine() {
        return this.commandLine;
    }

    public String getComment() {
        return this.comment;
    }

    public String getStart() {
        return this.start;
    }

    public String getEnd() {
        return this.end;
    }

    public String getDuration() {
        return this.duration;
    }

    public BorgArchiveStats getStats() {
        return this.stats;
    }

    public BorgArchiveLimits getLimits() {
        return this.limits;
    }

    public String getHostname() {
        return this.hostname;
    }

    public String getUsername() {
        return this.username;
    }
}
