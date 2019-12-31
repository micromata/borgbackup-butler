package de.micromata.borgbutler.server.rest;

import de.micromata.borgbutler.BorgQueueStatistics;
import de.micromata.borgbutler.server.BorgVersion;

/**
 * Statistics of all the job queues, especially the number of total queued and running jobs.
 * This is used e. g. by the client for showing a badge near to the menu entry "job monitor" with the number
 * of Jobs in the queues.
 */
public class SystemInfo {
    private BorgQueueStatistics queueStatistics;

    private boolean configurationOK;

    private BorgVersion borgVersion;

    public BorgQueueStatistics getQueueStatistics() {
        return this.queueStatistics;
    }

    public boolean isConfigurationOK() {
        return this.configurationOK;
    }

    public BorgVersion getBorgVersion() {
        return this.borgVersion;
    }

    public SystemInfo setQueueStatistics(BorgQueueStatistics queueStatistics) {
        this.queueStatistics = queueStatistics;
        return this;
    }

    public SystemInfo setConfigurationOK(boolean configurationOK) {
        this.configurationOK = configurationOK;
        return this;
    }

    public SystemInfo setBorgVersion(BorgVersion borgVersion) {
        this.borgVersion = borgVersion;
        return this;
    }
}
