package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.json.borg.BorgFilesystemItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FilesystemItem extends BorgFilesystemItem implements Serializable {
    private transient static Logger log = LoggerFactory.getLogger(FilesystemItem.class);
    private static final long serialVersionUID = 6561019300264543523L;

    @Getter
    @Setter
    private List<FilesystemItem> childs;
    @Getter
    @Setter
    private String name;

    public FilesystemItem() {

    }

    public FilesystemItem(BorgFilesystemItem item) {
        this.path = item.getPath();
        this.mode = item.getMode();
        this.mtime = item.getMtime();
        this.flags = item.getFlags();
        this.size = item.getSize();
        this.type = item.getType();
        this.gid = item.getGid();
        this.group = item.getGroup();
        this.healthy = item.isHealthy();
        this.linktarget = item.getLinktarget();
        this.source = item.getSource();
        this.uid = item.getUid();
        this.user = item.getUser();
    }


    /**
     * If running in diff mode, this flag specifies the type of difference. Null represents unmodified.
     */
    public enum DiffStatus {NEW, REMOVED, MODIFIED}

    @Setter
    @Getter
    protected String displayPath;

    /**
     * If created by diff tool, this flag represents the type of difference.
     */
    @Getter
    @Setter
    private FilesystemItem.DiffStatus diffStatus;
    /**
     * If created by diff tool, this object holds the file item of the other archive (diff archive).
     */
    @Getter
    @Setter
    private FilesystemItem diffItem;
    /**
     * If created by diff tool, this String contains all differences between current and other item for {@link FilesystemItem.DiffStatus#MODIFIED}.
     * This String may used for displaying.
     */
    @Getter
    private String differences;

    /**
     * Compares all fields and creates human readable string with differences.
     */
    public void buildDifferencesString() {
        if (diffItem == null) {
            // Nothing to do.
            return;
        }
        if (!StringUtils.equals(this.path, diffItem.getPath())) {
            log.error("*** Internal error: Differences should only be made on same path object: current='" + path
                    + "', other='" + diffItem.getPath() + "'.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        appendDiff(sb, "type", this.type, diffItem.getType());
        //appendDiff(sb, "mode", this.mode, diffItem.mode); // Done by frontend (jsx)
        appendDiff(sb, "user", this.user, diffItem.getUser());
        appendDiff(sb, "group", this.group, diffItem.getGroup());
        appendDiff(sb, "uid", this.uid, diffItem.getUid());
        appendDiff(sb, "gid", this.gid, diffItem.getGid());
        //appendDiff(sb, "mtime", this.mtime, diffItem.mtime); // Done by frontend (jsx)
        //appendDiff(sb, "size", this.size, diffItem.size); // Done by frontend (jsx)
        if (sb.length() > 0) {
            diffStatus = FilesystemItem.DiffStatus.MODIFIED;
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
        sb.append(field + ": ['" + current + "' -> '" + other + "']");
    }

    @Override
    public String toString() {
        return path;
    }

    FilesystemItem add(Path path, int count, BorgFilesystemItem borgItem) {
        if (childs == null) {
            childs = new ArrayList<>();
        }
        if (count + 1 == path.getNameCount()) {
            FilesystemItem item = new FilesystemItem(borgItem);
            item.setName(path.getFileName().toString());
            childs.add(item);
            return item;
        }
        String name = path.getName(count).toString();
        FilesystemItem child = null;
        for (FilesystemItem ch : childs) {
            if (StringUtils.equals(ch.name, name)) {
                child = ch;
                break;
            }
        }
        if (child == null) {
            child = new FilesystemItem();
            child.setName(name);
            childs.add(child);
        }
        return child.add(path, count + 1, borgItem);
    }

    public FilesystemItem find(String pathString) {
        if (StringUtils.isBlank(pathString)) {
            return this;
        }
        Path path = Paths.get(pathString);
        if (path.getNameCount() == 0) {
            return this;
        }
        return find(path, 0);
    }

    private FilesystemItem find(Path path, int count) {
        if (childs == null) {
            return null;
        }
        for (FilesystemItem child : childs) {
            String name = path.getName(count).toString();
            if (StringUtils.equals(child.name, name)) {
                if (path.getNameCount() == count + 1)
                    return child;
                else
                    return child.find(path, count + 1);
            }
        }
        return null;
    }

    public int getFileNumber() {
        return path.hashCode();
    }
}