package de.micromata.borgbutler.server.rest

import de.micromata.borgbutler.server.BorgVersion
import de.micromata.borgbutler.server.ServerConfiguration

class ConfigurationInfo(
    var serverConfiguration: ServerConfiguration? = null,
    var borgVersion: BorgVersion? = null
)
