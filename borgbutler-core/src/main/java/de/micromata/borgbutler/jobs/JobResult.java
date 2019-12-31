package de.micromata.borgbutler.jobs;

public class JobResult<T> {
    public Status getStatus() {
        return this.status;
    }

    public T getResultObject() {
        return this.resultObject;
    }

    public String getErrorString() {
        return this.errorString;
    }

    public JobResult<T> setStatus(Status status) {
        this.status = status;
        return this;
    }

    public JobResult<T> setResultObject(T resultObject) {
        this.resultObject = resultObject;
        return this;
    }

    public JobResult<T> setErrorString(String errorString) {
        this.errorString = errorString;
        return this;
    }

    public enum Status {OK, ERROR}
    private Status status;
    private T resultObject;
    private String errorString;
}
