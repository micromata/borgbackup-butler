package de.micromata.borgbutler.jobs;

import lombok.Getter;
import lombok.Setter;

public class JobResult<T> {
    public enum Status {OK, ERROR}
    @Getter
    @Setter
    private Status status;
    @Getter
    @Setter
    private T resultObject;
    @Getter
    @Setter
    private String errorString;
}
