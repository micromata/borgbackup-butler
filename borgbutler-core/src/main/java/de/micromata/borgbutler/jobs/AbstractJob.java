package de.micromata.borgbutler.jobs;

import de.micromata.borgbutler.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class AbstractJob<T> {
    private Logger logger = LoggerFactory.getLogger(AbstractJob.class);

    public boolean isCancellationRequested() {
        return this.cancellationRequested;
    }

    public Status getStatus() {
        return this.status;
    }

    public String getTitle() {
        return this.title;
    }

    Future<JobResult<T>> getFuture() {
        return this.future;
    }

    public long getUniqueJobNumber() {
        return this.uniqueJobNumber;
    }

    public String getCreateTime() {
        return this.createTime;
    }

    public String getStartTime() {
        return this.startTime;
    }

    public String getStopTime() {
        return this.stopTime;
    }

    public AbstractJob<T> setCancellationRequested(boolean cancellationRequested) {
        this.cancellationRequested = cancellationRequested;
        return this;
    }

    public AbstractJob<T> setTitle(String title) {
        this.title = title;
        return this;
    }

    AbstractJob<T> setFuture(Future<JobResult<T>> future) {
        this.future = future;
        return this;
    }

    protected AbstractJob<T> setUniqueJobNumber(long uniqueJobNumber) {
        this.uniqueJobNumber = uniqueJobNumber;
        return this;
    }

    public AbstractJob<T> setCreateTime(String createTime) {
        this.createTime = createTime;
        return this;
    }

    public AbstractJob<T> setStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public AbstractJob<T> setStopTime(String stopTime) {
        this.stopTime = stopTime;
        return this;
    }

    public enum Status {DONE, RUNNING, QUEUED, CANCELLED, FAILED}

    private boolean cancellationRequested;
    private Status status;
    private String title;
    private Future<JobResult<T>> future;
    private long uniqueJobNumber = -1;
    private String createTime;
    private String startTime;
    private String stopTime;

    protected AbstractJob<T> setStatus(Status status) {
        if (status == Status.RUNNING && this.status != Status.RUNNING) {
            this.startTime = DateUtils.format(LocalDateTime.now());
        } else if (status != Status.RUNNING && this.status == Status.RUNNING) {
            this.stopTime = DateUtils.format(LocalDateTime.now());
        }
        this.status = status;
        return this;
    }

    public void cancel() {
        if (this.getStatus() == Status.QUEUED) {
            setStatus(Status.CANCELLED);
        }
        this.cancellationRequested = true;
        cancelRunningProcess();
    }

    protected void setCancelled() {
        setStatus(Status.CANCELLED);
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
        if (this.status != null && this.status != Status.RUNNING) {
            logger.error("Internal error, illegal state! You shouldn't set the job status to FAILED if not in status RUNNING: " + this.status);
        }
        setStatus(Status.FAILED);
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

    protected AbstractJob() {
        this.createTime = DateUtils.format(LocalDateTime.now());
    }
}
