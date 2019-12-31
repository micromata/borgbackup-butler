package de.micromata.borgbutler;

/**
 * Statistics of all the job queues, especially the number of total queued and running jobs.
 * This is used e. g. by the client for showing a badge near to the menu entry "job monitor" with the number
 * of Jobs in the queues.
 */
public class BorgQueueStatistics {
    int numberOfRunningAndQueuedJobs = 0;
    int numberOfOldJobs = 0;
    int numberOfActiveQueues = 0;
    int totalNumberOfQueues = 0;

    public int getNumberOfRunningAndQueuedJobs() {
        return this.numberOfRunningAndQueuedJobs;
    }

    public int getNumberOfOldJobs() {
        return this.numberOfOldJobs;
    }

    public int getNumberOfActiveQueues() {
        return this.numberOfActiveQueues;
    }

    public int getTotalNumberOfQueues() {
        return this.totalNumberOfQueues;
    }
}
