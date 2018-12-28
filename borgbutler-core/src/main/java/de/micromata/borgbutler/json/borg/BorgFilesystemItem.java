package de.micromata.borgbutler.json.borg;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class BorgFilesystemItem implements Serializable, Comparable<BorgFilesystemItem> {
    private static final long serialVersionUID = -5545350851640655468L;

    /**
     * d (directory), - (file)
     */
    @Getter
    @Setter
    protected String type;
    /**
     * Unix mode, e. g. <tt>drwxr-xr-x</tt>
     */
    @Getter
    @Setter
    protected String mode;
    @Getter
    protected String user;
    @Getter
    protected String group;
    @Getter
    protected long uid;
    @Getter
    protected long gid;
    @Getter
    @Setter
    protected String path;
    @Getter
    protected boolean healthy;
    @Getter
    protected String source;
    @Getter
    protected String linktarget;
    @Getter
    protected String flags;
    @Getter
    @Setter
    protected String mtime;
    @Getter
    @Setter
    protected long size;

    @Override
    public int compareTo(BorgFilesystemItem o) {
        if (path == o.path) {
            return 0;
        }
        if (path == null) {
            return -1;
        }
        if (o.path == null) {
            return 1;
        }
        return path.compareToIgnoreCase(o.path);
    }
}
