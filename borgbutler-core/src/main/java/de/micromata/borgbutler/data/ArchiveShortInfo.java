package de.micromata.borgbutler.data;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * Used e. g. for the drop down box for comparing current archive with others.
 */
public class ArchiveShortInfo implements Serializable, Comparable<ArchiveShortInfo> {
    /**
     * For convenience purposes for the client.
     */
    private String repoName;
    private String repoId;
    private String name;
    private String id;
    private String time;
    /**
     * Is the file list of this archive loaded and available in Butler's cache.
     */
    private boolean fileListAlreadyCached;

    public ArchiveShortInfo() {

    }

    public ArchiveShortInfo(Archive archive) {
        this.id = archive.getId();
        this.name = archive.getName();
        this.repoId = archive.getRepoId();
        this.time = archive.getTime();
        this.fileListAlreadyCached = archive.isFileListAlreadyCached();
    }

    /**
     * In reverse order, compares times.
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(ArchiveShortInfo o) {
        // Reverse order:
        return StringUtils.compare(o.time, this.time);
    }

    public String getRepoName() {
        return this.repoName;
    }

    public String getRepoId() {
        return this.repoId;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public String getTime() {
        return this.time;
    }

    public boolean isFileListAlreadyCached() {
        return this.fileListAlreadyCached;
    }

    public ArchiveShortInfo setRepoName(String repoName) {
        this.repoName = repoName;
        return this;
    }

    public ArchiveShortInfo setRepoId(String repoId) {
        this.repoId = repoId;
        return this;
    }

    public ArchiveShortInfo setName(String name) {
        this.name = name;
        return this;
    }

    public ArchiveShortInfo setId(String id) {
        this.id = id;
        return this;
    }

    public ArchiveShortInfo setTime(String time) {
        this.time = time;
        return this;
    }

    public ArchiveShortInfo setFileListAlreadyCached(boolean fileListAlreadyCached) {
        this.fileListAlreadyCached = fileListAlreadyCached;
        return this;
    }
}
