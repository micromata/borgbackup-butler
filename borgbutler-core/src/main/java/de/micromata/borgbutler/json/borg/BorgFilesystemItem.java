package de.micromata.borgbutler.json.borg;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class BorgFilesystemItem implements Serializable, Comparable<BorgFilesystemItem> {
    private transient static Logger log = LoggerFactory.getLogger(BorgFilesystemItem.class);
    private static final long serialVersionUID = -5545350851640655468L;

    public String getType() {
        return this.type;
    }

    public String getMode() {
        return this.mode;
    }

    public String getUser() {
        return this.user;
    }

    public String getGroup() {
        return this.group;
    }

    public long getUid() {
        return this.uid;
    }

    public long getGid() {
        return this.gid;
    }

    public String getPath() {
        return this.path;
    }

    public String getDisplayPath() {
        return this.displayPath;
    }

    public boolean isHealthy() {
        return this.healthy;
    }

    public String getSource() {
        return this.source;
    }

    public String getLinktarget() {
        return this.linktarget;
    }

    public String getFlags() {
        return this.flags;
    }

    public String getMtime() {
        return this.mtime;
    }

    public long getSize() {
        return this.size;
    }

    public int getFileNumber() {
        return this.fileNumber;
    }

    public DiffStatus getDiffStatus() {
        return this.diffStatus;
    }

    public BorgFilesystemItem getDiffItem() {
        return this.diffItem;
    }

    public String getDifferences() {
        return this.differences;
    }

    public BorgFilesystemItem setType(String type) {
        this.type = type;
        return this;
    }

    public BorgFilesystemItem setMode(String mode) {
        this.mode = mode;
        return this;
    }

    public BorgFilesystemItem setUser(String user) {
        this.user = user;
        return this;
    }

    public BorgFilesystemItem setUid(long uid) {
        this.uid = uid;
        return this;
    }

    public BorgFilesystemItem setPath(String path) {
        this.path = path;
        return this;
    }

    public BorgFilesystemItem setDisplayPath(String displayPath) {
        this.displayPath = displayPath;
        return this;
    }

    public BorgFilesystemItem setMtime(String mtime) {
        this.mtime = mtime;
        return this;
    }

    public BorgFilesystemItem setSize(long size) {
        this.size = size;
        return this;
    }

    public BorgFilesystemItem setFileNumber(int fileNumber) {
        this.fileNumber = fileNumber;
        return this;
    }

    public BorgFilesystemItem setDiffStatus(DiffStatus diffStatus) {
        this.diffStatus = diffStatus;
        return this;
    }

    public BorgFilesystemItem setDiffItem(BorgFilesystemItem diffItem) {
        this.diffItem = diffItem;
        return this;
    }

    /**
     * If running in diff mode, this flag specifies the type of difference. Null represents unmodified.
     */
    public enum DiffStatus {NEW, REMOVED, MODIFIED}

    /**
     * d (directory), - (file)
     */
    private String type;
    /**
     * Unix mode, e. g. <tt>drwxr-xr-x</tt>
     */
    private String mode;
    private String user;
    private String group;
    private long uid;
    private long gid;
    private String path;
    private String displayPath;
    private boolean healthy;
    private String source;
    private String linktarget;
    private String flags;
    private String mtime;
    private long size;
    /**
     * Represents the number of the file in the archive (for downloading). This field is created and only known by BorgButler.
     */
    private int fileNumber = -1;

    /**
     * If created by diff tool, this flag represents the type of difference.
     */
    private DiffStatus diffStatus;
    /**
     * If created by diff tool, this object holds the file item of the other archive (diff archive).
     */
    private BorgFilesystemItem diffItem;
    /**
     * If created by diff tool, this String contains all differences between current and other item for {@link DiffStatus#MODIFIED}.
     * This String may used for displaying.
     */
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

    @Override
    public BorgFilesystemItem clone() {
        BorgFilesystemItem clone = new BorgFilesystemItem();
        clone.type = this.type;
        clone.mode = this.mode;
        clone.user = this.user;
        clone.group = this.group;
        clone.uid = this.uid;
        clone.gid = this.gid;
        clone.path = this.path;
        clone.displayPath = this.displayPath;
        clone.healthy = this.healthy;
        clone.source = this.source;
        clone.linktarget = this.linktarget;
        clone.flags = this.flags;
        clone.mtime = this.mtime;
        clone.size = this.size;
        clone.fileNumber = this.fileNumber;
        clone.diffStatus = this.diffStatus;
        clone.diffItem = this.diffItem;
        clone.differences = this.differences;
        return clone;
    }
}
