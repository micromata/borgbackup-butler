package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Result of <tt>borg list repo</tt>.
 */
public class ArchiveList extends RepositoryMatcher {
    @Getter
    private List<Archive2> archives;
    @Getter
    private Cache cache;
    @Getter
    private Encryption encryption;
    @Getter
    @Setter
    @JsonIgnore
    private String originalJson;

    public void updateFrom(ArchiveList archiveList) {
        super.updateFrom(archiveList);
        this.archives = archiveList.archives;
        this.cache = archiveList.cache;
        this.encryption = archiveList.encryption;
    }
}
