package de.micromata.borgbutler.server.rest

import de.micromata.borgbutler.BorgQueueStatistics
import de.micromata.borgbutler.server.BorgVersion

/**
 * Statistics of all the job queues, especially the number of total queued and running jobs.
 * This is used e. g. by the client for showing a badge near to the menu entry "job monitor" with the number
 * of Jobs in the queues.
 */
class SystemInfo {
    var queueStatistics: BorgQueueStatistics? = null
        private set
    var isConfigurationOK = false
        private set
    var borgVersion: BorgVersion? = null
        private set

    fun setQueueStatistics(queueStatistics: BorgQueueStatistics?): SystemInfo {
        this.queueStatistics = queueStatistics
        return this
    }

    fun setConfigurationOK(configurationOK: Boolean): SystemInfo {
        isConfigurationOK = configurationOK
        return this
    }

    fun setBorgVersion(borgVersion: BorgVersion?): SystemInfo {
        this.borgVersion = borgVersion
        return this
    }
}
