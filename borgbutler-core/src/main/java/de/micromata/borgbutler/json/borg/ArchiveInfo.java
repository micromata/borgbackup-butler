package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Result of <tt>borg info repo::archive</tt>.
 */
public class ArchiveInfo implements Serializable {
    private static final long serialVersionUID = -4200553322856662346L;
    @Getter
    private List<Archive2> archives;
    @Getter
    private Cache cache;
    @Getter
    private Encryption encryption;
    @Getter
    private Repository repository;
    @Getter
    @Setter
    @JsonIgnore
    private String originalJson;
}
