package de.micromata.borgbutler.json.borg;

import de.micromata.borgbutler.json.JsonUtils;
import lombok.Getter;

import java.io.Serializable;

/**
 * This object is given by <tt>borg list repo</tt>.
 */
public class BorgArchive implements Serializable {
    private static final long serialVersionUID = -7872260170265536732L;
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

    public String toString() {
        return JsonUtils.toJson(this, true);
    }
}
