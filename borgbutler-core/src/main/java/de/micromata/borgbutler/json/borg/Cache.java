package de.micromata.borgbutler.json.borg;

import lombok.Getter;

import java.io.Serializable;

public class Cache implements Serializable {
    private static final long serialVersionUID = -1728825838475013561L;
    @Getter
    private String path;
    @Getter
    private Stats stats;
}
