package de.micromata.borgbutler;

import de.micromata.borgbutler.jobs.JobResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Holder of result object of borg commands. Holds the result object as well as error messages and status.
 */
public class BorgCommandResult<T> {

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private T object;

    @Getter
    @Setter(AccessLevel.PACKAGE)
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
}
