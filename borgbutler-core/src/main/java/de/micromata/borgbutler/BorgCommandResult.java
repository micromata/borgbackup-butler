package de.micromata.borgbutler;

import de.micromata.borgbutler.jobs.JobResult;

/**
 * Holder of result object of borg commands. Holds the result object as well as error messages and status.
 */
public class BorgCommandResult<T> {

    private T object;

    private JobResult<String> jobResult;

    public JobResult.Status getStatus() {
        return jobResult != null ? jobResult.getStatus() : JobResult.Status.ERROR;
    }

    public String getError() {
        if (jobResult != null) {
            return jobResult.getErrorString();
        }
        return "Unkown error... (please refer the log files)";
    }

    public T getObject() {
        return this.object;
    }

    public JobResult<String> getJobResult() {
        return this.jobResult;
    }

    BorgCommandResult<T> setObject(T object) {
        this.object = object;
        return this;
    }

    BorgCommandResult<T> setJobResult(JobResult<String> jobResult) {
        this.jobResult = jobResult;
        return this;
    }
}
