package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Result of <tt>borg info repo::archive</tt>.
 */
public class BorgArchiveInfo implements Serializable {
    private static final long serialVersionUID = -4200553322856662346L;
    @Getter
    private List<BorgArchive2> archives;
    @Getter
    private BorgCache cache;
    @Getter
    private BorgEncryption encryption;
    @Getter
    private BorgRepository repository;
    @Getter
    @Setter
    @JsonIgnore
    private String originalJson;
}
