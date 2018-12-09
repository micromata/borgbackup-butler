package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.BorgCommands;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.json.borg.ArchiveInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ArchiveListCache extends AbstractElementsCache<ArchiveInfo> {
    private static Logger log = LoggerFactory.getLogger(ArchiveListCache.class);
    public static final String CACHE_ARCHIVE_LISTS_BASENAME = "archive-lists";

    @Override
    protected ArchiveInfo load(BorgRepoConfig repoConfig, String identifier) {
        ArchiveInfo archiveInfo = BorgCommands.info(repoConfig, identifier);
        this.elements.put(getIdentifier(archiveInfo), archiveInfo);
        return archiveInfo;
    }

    @Override
    protected boolean matches(ArchiveInfo element, String identifier) {
        return element.matches(identifier);
    }

    @Override
    protected String getIdentifier(ArchiveInfo element) {
        return element.getRepository().getId();
    }

    @Override
    protected void updateFrom(ArchiveInfo dest, ArchiveInfo source) {
        dest.updateFrom(source);
    }

    /**
     * Needed by jackson for deserialization.
     */
    ArchiveListCache() {
    }

    ArchiveListCache(File cacheDir) {
        super(cacheDir, CACHE_ARCHIVE_LISTS_BASENAME);
    }
}
