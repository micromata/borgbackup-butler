package de.micromata.borgbutler.json.borg;

import lombok.Getter;

public class FilesystemItem {
    /**
     * d (directory), - (file)
     */
    @Getter
    private String type;
    /**
     * Unix mode, e. g. <tt>drwxr-xr-x</tt>
     */
    @Getter
    private String mode;
    @Getter
    private String user;
    @Getter
    private String group;
    @Getter
    private long uid;
    @Getter
    private long gid;
    @Getter
    private String path;
    @Getter
    private boolean healthy;
    @Getter
    private String source;
    @Getter
    private String linktarget;
    @Getter
    private String flags;
    @Getter
    private String mtime;
    @Getter
    private long size;
}
