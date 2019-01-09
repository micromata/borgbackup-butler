package de.micromata.borgbutler.json.borg;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class BorgFilesystemItem implements Serializable, Comparable<BorgFilesystemItem> {
    private transient static Logger log = LoggerFactory.getLogger(BorgFilesystemItem.class);
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
    @Setter
    private String mode;
    @Getter
    @Setter
    private String user;
    @Getter
    private String group;
    @Getter
    @Setter
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
    @Setter
    private long size;
    /**
     * Represents the number of the file in the archive (for downloading). This field is created and only known by BorgButler.
     */
    @Getter
    @Setter
    private int fileNumber;
    /**
     * Represents the number of the file in the archive of the parent (for faster filterin). This field is created and only known by BorgButler.
     */
    @Getter
    @Setter
    private Integer parentFileNumber;

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
            return 1;
        }
        return path.compareToIgnoreCase(o.path);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        BorgFilesystemItem rhs = (BorgFilesystemItem) obj;
        return new EqualsBuilder()
                .append(path, rhs.path)
                .append(type, rhs.type)
                .append(mode, rhs.mode)
                .append(user, rhs.user)
                .append(group, rhs.group)
                .append(uid, rhs.uid)
                .append(gid, rhs.gid)
                .append(mtime, rhs.mtime)
                .append(size, rhs.size)
                .append(flags, rhs.flags)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(path)
                .append(type)
                .append(mode)
                .append(user)
                .append(group)
                .append(uid)
                .append(gid)
                .append(mtime)
                .append(size)
                .append(flags)
                .toHashCode();
    }

    /**
     * Compares all fields and creates human readable string with differences.
     */
    public void buildDifferencesString() {
        if (diffItem == null) {
            // Nothing to do.
            return;
        }
        if (!StringUtils.equals(this.path, diffItem.path)) {
            log.error("*** Internal error: Differences should only be made on same path object: current='" + path + "', other='" + diffItem.path + "'.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        appendDiff(sb, "type", this.type, diffItem.type);
        //appendDiff(sb, "mode", this.mode, diffItem.mode); // Done by frontend (jsx)
        appendDiff(sb, "user", this.user, diffItem.user);
        appendDiff(sb, "group", this.group, diffItem.group);
        appendDiff(sb, "uid", this.uid, diffItem.uid);
        appendDiff(sb, "gid", this.gid, diffItem.gid);
        //appendDiff(sb, "mtime", this.mtime, diffItem.mtime); // Done by frontend (jsx)
        //appendDiff(sb, "size", this.size, diffItem.size); // Done by frontend (jsx)
        if (sb.length() > 0) {
            diffStatus = DiffStatus.MODIFIED;
            this.differences = sb.toString();
        }
    }

    private void appendDiff(StringBuilder sb, String field, String current, String other) {
        if (StringUtils.equals(current, other)) {
            // Not modified.
            return;
        }
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append(field + ":['" + other + "'->'" + current + "']");
    }

    private void appendDiff(StringBuilder sb, String field, long current, long other) {
        if (current == other) {
            // Not modified.
            return;
        }
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append(field + ":['" + other + "'->'" + current + "']");
    }

    @Override
    public String toString() {
        return path;
    }

    /**
     * @return if this item is of type directory the path is returned, otherwise the directory where this item is in.
     */
    public String _getInternalDirectory() {
        if (_internalDirectory != null) {
            return _internalDirectory;
        }
        if ("d".equals(type)) {
            return _internalDirectory = path;
        } else {
            return _internalDirectory = FilenameUtils.getPath(path);
        }
    }

    public boolean _isInternalDirectorySet() {
        return _internalDirectory != null;
    }

    public void _setInternalDirectory(String directory) {
        _internalDirectory = directory;
    }

    public String getFullPath() {
        if (_internalDirectory == null) {
            if (parentFileNumber != null) {
                System.err.println("**** Internal error: oups, shouldn't occur.");
                return null;
            } else {
                return path;
            }
        }
        if ("d".equals(type)) return _internalDirectory;
        return _internalDirectory + path;
    }

    /**
     * For preventing multiple calculations in {@link #_getInternalDirectory()}.
     */
    private transient String _internalDirectory;

    public boolean isDirectory() {
        return "d".equals(type);
    }
}
