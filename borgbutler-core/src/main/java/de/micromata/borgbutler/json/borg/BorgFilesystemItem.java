package de.micromata.borgbutler.json.borg;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public class BorgFilesystemItem implements Serializable, Comparable<BorgFilesystemItem> {
    private static final long serialVersionUID = -5545350851640655468L;

    /**
     * If running in diff mode, this flag specifies the type of difference. Null represents unmodified.
     */
    public enum DiffStatus {NEW, REMOVED, MODIFIED}
    /**
     * d (directory), - (file)
     */
    @Getter
    @Setter
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
    @Setter
    private String path;
    @Setter
    @Getter
    private String displayPath;
    @Getter
    private boolean healthy;
    @Getter
    private String source;
    @Getter
    private String linktarget;
    @Getter
    private String flags;
    @Getter
    @Setter
    private String mtime;
    @Getter
    private long size;
    /**
     * Represents the number of the file in the archive (for downloading). This field is created and only known by BorgButler.
     */
    @Getter
    @Setter
    private int fileNumber;
    /**
     * If created by diff tool, this flag represents the type of difference.
     */
    @Getter
    @Setter
    private DiffStatus diffStatus;
    /**
     * If created by diff tool, this object holds the file item of the other archive (diff archive).
     */
    @Getter
    @Setter
    private BorgFilesystemItem diffItem;
    /**
     * If created by diff tool, this String contains all differences between current and other item for {@link DiffStatus#MODIFIED}.
     * This String may used for displaying.
     */
    @Getter
    @Setter
    private String differences;

    @Override
    public int compareTo(BorgFilesystemItem o) {
        if (path == o.path) {
            return 0;
        }
        if (path == null) {
            return -1;
        }
        if (o.path == null) {
            return  1;
        }
        return path.compareToIgnoreCase(o.path);
    }
}
