package de.micromata.borgbutler.json.borg;

import java.io.Serializable;

public class BorgCache implements Serializable {
    private static final long serialVersionUID = -1728825838475013561L;
    private String path;
    private BorgStats stats;

    public String getPath() {
        return this.path;
    }

    public BorgStats getStats() {
        return this.stats;
    }
}
