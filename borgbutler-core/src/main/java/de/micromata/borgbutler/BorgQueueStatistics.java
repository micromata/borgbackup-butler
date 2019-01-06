package de.micromata.borgbutler;

import lombok.Getter;

/**
 * Statistics of all the job queues, especially the number of total queued and running jobs.
 * This is used e. g. by the client for showing a badge near to the menu entry "job monitor" with the number
 * of Jobs in the queues.
 */
public class BorgQueueStatistics {
    @Getter
    int numberOfRunningAndQueuedJobs = 0;
    @Getter
    int numberOfOldJobs = 0;
    @Getter
    int numberOfActiveQueues = 0;
    @Getter
    int totalNumberOfQueues = 0;
}
