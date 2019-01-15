package de.micromata.borgbutler.server.rest;

import de.micromata.borgbutler.BorgQueueStatistics;
import de.micromata.borgbutler.server.BorgVersion;
import lombok.Getter;
import lombok.Setter;

/**
 * Statistics of all the job queues, especially the number of total queued and running jobs.
 * This is used e. g. by the client for showing a badge near to the menu entry "job monitor" with the number
 * of Jobs in the queues.
 */
public class SystemInfo {
    @Getter
    @Setter
    private BorgQueueStatistics queueStatistics;

    @Getter
    @Setter
    private boolean configurationOK;

    @Getter
    @Setter
    private BorgVersion borgVersion;
}
