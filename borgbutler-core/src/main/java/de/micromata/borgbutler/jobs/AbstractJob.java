package de.micromata.borgbutler.jobs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJob {
    private Logger logger = LoggerFactory.getLogger(AbstractJob.class);
    public enum Status {DONE, RUNNING, QUEUED, STOPPED, FAILED}
    @Getter
    @Setter
    private boolean stopRequested;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Status status;
    @Getter
    @Setter
    private String title;
    @Getter
    @Setter
    private String statusText;

    protected void failed() {
        if (this.status != Status.RUNNING) {
            logger.error("Internal error, illegal state! You shouldn't set the job status to FAILED if not in status RUNNING: " + this.status);
        }
        this.status = Status.FAILED;
    }
    /**
     *
     * @return true, if the job is done, stopped or failed. Otherwise false (if job is running or queued).
     */
    public boolean isFinished() {
        if (status == Status.DONE || status == Status.STOPPED || status == Status.FAILED) {
            return true;
        }
        return false;
    }

    public abstract void execute();

    /**
     * A job is identified by this id. If a job with the same id is already queued (not yet finished), this job will
     * not be added twice.
     * @return
     */
    public abstract Object getId();
}
