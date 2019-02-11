package de.micromata.borgbutler.server.rest;

import de.micromata.borgbutler.server.BorgVersion;
import de.micromata.borgbutler.server.ServerConfiguration;
import lombok.Getter;
import lombok.Setter;

public class ConfigurationInfo {
    @Getter
    @Setter
    private ServerConfiguration serverConfiguration;
    @Getter
    @Setter
    private BorgVersion borgVersion;
}
