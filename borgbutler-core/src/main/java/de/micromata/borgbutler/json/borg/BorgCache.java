package de.micromata.borgbutler.json.borg;

import lombok.Getter;

import java.io.Serializable;

public class BorgCache implements Serializable {
    private static final long serialVersionUID = -1728825838475013561L;
    @Getter
    private String path;
    @Getter
    private BorgStats stats;
}
