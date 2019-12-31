package de.micromata.borgbutler.data;

import de.micromata.borgbutler.json.borg.BorgArchiveLimits;
import de.micromata.borgbutler.json.borg.BorgArchiveStats;
import de.micromata.borgbutler.json.borg.BorgCache;
import de.micromata.borgbutler.json.borg.BorgEncryption;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 *
 */
public class Archive implements Serializable, Comparable<Archive> {
    /**
     * For convenience purposes for the client.
     */
    private String repoName;
    /**
     * For convenience purposes for the client.
     */
    private String repoDisplayName;
    /**
     * For convenience purposes for the client.
     */
    private String repoId;
    private String name;
    private String id;
    private BorgCache cache;
    private BorgEncryption encryption;

    private int[] chunkerParams;
    /**
     * The command line used for creating this archive: borg create --filter...
     */
    private String[] commandLine;
    private String comment;
    private String start;
    private String end;
    private String time;
    private String duration;
    private BorgArchiveStats stats;
    private BorgArchiveLimits limits;
    private String username;
    private String hostname;
    /**
     * For comparing functionality.
     */
    private List<ArchiveShortInfo> archiveShortInfoList;
    /**
     * Is the file list of this archive loaded and available in Butler's cache.
     */
    private boolean fileListAlreadyCached;

    /**
     *
     * @return repoName::archiveName
     */
    public String getBorgIdentifier() {
        return repoName + "::" + name;
    }

    /**
     * Is <tt>borg info repo::archive</tt> already called for this archive?
     *
     * @return true, if borg info was called, otherwise false.
     */
    public boolean hasInfoData() {
        return commandLine != null && commandLine.length > 0;
    }

    /**
     * In reverse order, compares times.
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Archive o) {
        // Reverse order:
        return StringUtils.compare(o.time, this.time);
    }

    public String getRepoName() {
        return this.repoName;
    }

    public String getRepoDisplayName() {
        return this.repoDisplayName;
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

    public BorgCache getCache() {
        return this.cache;
    }

    public BorgEncryption getEncryption() {
        return this.encryption;
    }

    public int[] getChunkerParams() {
        return this.chunkerParams;
    }

    public String[] getCommandLine() {
        return this.commandLine;
    }

    public String getComment() {
        return this.comment;
    }

    public String getStart() {
        return this.start;
    }

    public String getEnd() {
        return this.end;
    }

    public String getTime() {
        return this.time;
    }

    public String getDuration() {
        return this.duration;
    }

    public BorgArchiveStats getStats() {
        return this.stats;
    }

    public BorgArchiveLimits getLimits() {
        return this.limits;
    }

    public String getUsername() {
        return this.username;
    }

    public String getHostname() {
        return this.hostname;
    }

    public List<ArchiveShortInfo> getArchiveShortInfoList() {
        return this.archiveShortInfoList;
    }

    public boolean isFileListAlreadyCached() {
        return this.fileListAlreadyCached;
    }

    public Archive setRepoName(String repoName) {
        this.repoName = repoName;
        return this;
    }

    public Archive setRepoDisplayName(String repoDisplayName) {
        this.repoDisplayName = repoDisplayName;
        return this;
    }

    public Archive setRepoId(String repoId) {
        this.repoId = repoId;
        return this;
    }

    public Archive setName(String name) {
        this.name = name;
        return this;
    }

    public Archive setId(String id) {
        this.id = id;
        return this;
    }

    public Archive setCache(BorgCache cache) {
        this.cache = cache;
        return this;
    }

    public Archive setEncryption(BorgEncryption encryption) {
        this.encryption = encryption;
        return this;
    }

    public Archive setChunkerParams(int[] chunkerParams) {
        this.chunkerParams = chunkerParams;
        return this;
    }

    public Archive setCommandLine(String[] commandLine) {
        this.commandLine = commandLine;
        return this;
    }

    public Archive setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public Archive setStart(String start) {
        this.start = start;
        return this;
    }

    public Archive setEnd(String end) {
        this.end = end;
        return this;
    }

    public Archive setTime(String time) {
        this.time = time;
        return this;
    }

    public Archive setDuration(String duration) {
        this.duration = duration;
        return this;
    }

    public Archive setStats(BorgArchiveStats stats) {
        this.stats = stats;
        return this;
    }

    public Archive setLimits(BorgArchiveLimits limits) {
        this.limits = limits;
        return this;
    }

    public Archive setUsername(String username) {
        this.username = username;
        return this;
    }

    public Archive setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public Archive setArchiveShortInfoList(List<ArchiveShortInfo> archiveShortInfoList) {
        this.archiveShortInfoList = archiveShortInfoList;
        return this;
    }

    public Archive setFileListAlreadyCached(boolean fileListAlreadyCached) {
        this.fileListAlreadyCached = fileListAlreadyCached;
        return this;
    }
}
