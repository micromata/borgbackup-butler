package de.micromata.borgbutler.json.borg;

import de.micromata.borgbutler.json.JsonUtils;

import java.io.Serializable;

/**
 * This object is given by <tt>borg list repo</tt>.
 */
public class BorgArchive implements Serializable {
    private static final long serialVersionUID = -7872260170265536732L;
    private String archive;
    private String barchive;
    private String id;
    private String name;
    private String start;
    private String time;


    public String toString() {
        return JsonUtils.toJson(this, true);
    }

    public String getArchive() {
        return this.archive;
    }

    public String getBarchive() {
        return this.barchive;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getStart() {
        return this.start;
    }

    public String getTime() {
        return this.time;
    }
}
