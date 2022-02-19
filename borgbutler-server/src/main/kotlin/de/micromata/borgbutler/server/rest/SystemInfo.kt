package de.micromata.borgbutler.server.rest

import de.micromata.borgbutler.BorgQueueStatistics
import de.micromata.borgbutler.server.BorgConfig

/**
 * Statistics of all the job queues, especially the number of total queued and running jobs.
 * This is used e. g. by the client for showing a badge near to the menu entry "job monitor" with the number
 * of Jobs in the queues.
 */
class SystemInfo {
    var queueStatistics: BorgQueueStatistics? = null
        private set
    var configurationOK = false
        private set
    var borgConfig: BorgConfig? = null
        private set

    fun setQueueStatistics(queueStatistics: BorgQueueStatistics?): SystemInfo {
        this.queueStatistics = queueStatistics
        return this
    }

    fun setConfigurationOK(configurationOK: Boolean): SystemInfo {
        this.configurationOK = configurationOK
        return this
    }

    fun setBorgConfig(borgConfig: BorgConfig?): SystemInfo {
        this.borgConfig = borgConfig
        return this
    }
}
