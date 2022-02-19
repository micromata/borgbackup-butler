package de.micromata.borgbutler.server.rest

import de.micromata.borgbutler.server.BorgConfig
import de.micromata.borgbutler.server.ServerConfiguration

class ConfigurationInfo(
    var serverConfiguration: ServerConfiguration? = null,
    var borgConfig: BorgConfig? = null
)
