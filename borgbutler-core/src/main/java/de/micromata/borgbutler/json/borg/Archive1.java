package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.micromata.borgbutler.json.JsonUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * This object is given by <tt>borg list repo</tt>.
 */
public class Archive1 {
    @Getter
    private String archive;
    @Getter
    private String barchive;
    @Getter
    private String id;
    @Getter
    private String name;
    @Getter
    private String start;
    @Getter
    private String time;
    @Getter
    @Setter
    @JsonIgnore
    private String originalJson;

    public String toString() {
        return JsonUtils.toJson(this, true);
    }
}
