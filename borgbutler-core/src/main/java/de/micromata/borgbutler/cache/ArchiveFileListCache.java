package de.micromata.borgbutler.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.micromata.borgbutler.BorgCommands;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.json.borg.Archive;
import de.micromata.borgbutler.json.borg.FilesystemItem;
import de.micromata.borgbutler.utils.ReplaceUtils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

class ArchiveFileListCache extends AbstractCache {
    private static Logger log = LoggerFactory.getLogger(ArchiveFileListCache.class);
    public static final String CACHE_ARCHIVE_LISTS_BASENAME = "archive-content-";

    @JsonIgnore
    @Getter
    private Archive archive;
    @JsonProperty
    private List<FilesystemItem> content;

    public List<FilesystemItem> getContent(BorgRepoConfig repoConfig) {
        if (content == null) {
            read();
        }
        if (content == null) {
            this.content = BorgCommands.list(repoConfig, archive);
            save();
        }
        return content;
    }

    protected void update(AbstractCache readCache) {
        this.content = ((ArchiveFileListCache) readCache).content;
    }

    /**
     * Needed by jackson for deserialization.
     */
    ArchiveFileListCache() {
    }

    ArchiveFileListCache(File cacheDir, BorgRepoConfig repoConfig, Archive archive) {
        super(cacheDir, ReplaceUtils.encodeFilename(CACHE_ARCHIVE_LISTS_BASENAME + repoConfig.getName() + "-" + archive.getArchive(),
                true), true);
        this.archive = archive;
    }
}
