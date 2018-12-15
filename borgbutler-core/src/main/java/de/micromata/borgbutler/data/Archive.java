package de.micromata.borgbutler.data;

import de.micromata.borgbutler.json.borg.BorgArchiveStats;
import de.micromata.borgbutler.json.borg.BorgCache;
import de.micromata.borgbutler.json.borg.BorgEncryption;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 *
 */
public class Archive implements Serializable {
    @Getter
    @Setter
    private Repository repository;
    @Getter
    @Setter
    private BorgCache cache;
    @Getter
    @Setter
    private BorgEncryption encryption;

    @Getter
    @Setter
    private int[] chunkerParams;
    /**
     * The command line used for creating this archive: borg create --filter...
     */
    @Getter
    @Setter
    private String[] commandLine;
    @Getter
    @Setter
    private String comment;
    @Getter
    @Setter
    private String start;
    @Getter
    @Setter
    private BorgArchiveStats stats;

}
