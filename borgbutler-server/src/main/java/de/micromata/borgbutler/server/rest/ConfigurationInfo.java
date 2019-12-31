package de.micromata.borgbutler.server.rest;

import de.micromata.borgbutler.server.BorgVersion;
import de.micromata.borgbutler.server.ServerConfiguration;

public class ConfigurationInfo {
    private ServerConfiguration serverConfiguration;
    private BorgVersion borgVersion;

    public ServerConfiguration getServerConfiguration() {
        return this.serverConfiguration;
    }

    public BorgVersion getBorgVersion() {
        return this.borgVersion;
    }

    public ConfigurationInfo setServerConfiguration(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
        return this;
    }

    public ConfigurationInfo setBorgVersion(BorgVersion borgVersion) {
        this.borgVersion = borgVersion;
        return this;
    }
}
