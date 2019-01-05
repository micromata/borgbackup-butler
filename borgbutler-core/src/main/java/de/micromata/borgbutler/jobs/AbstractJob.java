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
    private boolean cancellationRequested;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Status status;
    @Getter
    @Setter
    private String title;
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private Future<JobResult<T>> future;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private long uniqueJobNumber = -1;

    public void cancel() {
        if (this.getStatus() == Status.QUEUED) {
            this.status = Status.CANCELLED;
        }
        this.cancellationRequested = true;
        cancelRunningProcess();
    }

    protected void setCancelled() {
        this.status = Status.CANCELLED;
    }

    /**
     * Not supported if not implemented.
     */
    protected void cancelRunningProcess() {
    }

    /**
     * Waits for and gets the result.
     *
     * @return
     */
    public JobResult<T> getResult() {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null;
    }

    public T getResultObject() {
        return getResult().getResultObject();
    }

    protected void failed() {
        if (this.status == Status.CANCELLED) {
            // do nothing. It's normal that cancelled jobs fail.
            return;
        }
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

    public abstract JobResult<T> execute();

    /**
     * A job is identified by this id. If a job with the same id is already queued (not yet finished), this job will
     * not be added twice.
     *
     * @return
     */
    public abstract Object getId();
}
