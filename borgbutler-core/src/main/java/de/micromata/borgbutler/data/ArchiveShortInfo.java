package de.micromata.borgbutler.data;

import de.micromata.borgbutler.json.borg.BorgArchiveLimits;
import de.micromata.borgbutler.json.borg.BorgArchiveStats;
import de.micromata.borgbutler.json.borg.BorgCache;
import de.micromata.borgbutler.json.borg.BorgEncryption;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * Used e. g. for the drop down box for comparing current archive with others.
 */
public class ArchiveShortInfo implements Serializable, Comparable<ArchiveShortInfo> {
    /**
     * For convenience purposes for the client.
     */
    @Getter
    @Setter
    private String repoName;
    @Getter
    @Setter
    private String repoId;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String id;
    @Getter
    @Setter
    private String time;
    /**
     * Is the file list of this archive loaded and available in Butler's cache.
     */
    @Getter
    @Setter
    private boolean fileListAlreadyCached;

    /**
     *
     * @return repoName::archiveName
     */
    public String getBorgIdentifier() {
        return repoName + "::" + name;
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
}
