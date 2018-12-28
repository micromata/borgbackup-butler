package de.micromata.borgbutler.jobs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class AbstractJob<T> {
    private Logger logger = LoggerFactory.getLogger(AbstractJob.class);

    public enum Status {DONE, RUNNING, QUEUED, CANCELLED, FAILED}

    @Getter
    @Setter
    private boolean cancelledRequested;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Status status;
    @Getter
    @Setter
    private String title;
    @Getter
    @Setter
    private String statusText;
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private Future<T> future;

    public void cancel() {
        if (this.getStatus() == Status.QUEUED) {
            this.status = Status.CANCELLED;
        }
        this.cancelledRequested = true;
    }

    /**
     * Waits for and gets the result.
     * @return
     */
    public T getResult() {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null;
    }

    protected void failed() {
        if (this.status != Status.RUNNING) {
            logger.error("Internal error, illegal state! You shouldn't set the job status to FAILED if not in status RUNNING: " + this.status);
        }
        this.status = Status.FAILED;
    }

    /**
     * @return true, if the job is done, stopped or failed. Otherwise false (if job is running or queued).
     */
    public boolean isFinished() {
        if (status == Status.DONE || status == Status.CANCELLED || status == Status.FAILED) {
            return true;
        }
        return false;
    }

    public abstract T execute();

    /**
     * A job is identified by this id. If a job with the same id is already queued (not yet finished), this job will
     * not be added twice.
     *
     * @return
     */
    public abstract Object getId();
}
