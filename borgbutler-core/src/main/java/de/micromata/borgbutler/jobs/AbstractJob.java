package de.micromata.borgbutler.jobs;

import lombok.Getter;
import lombok.Setter;

public abstract class AbstractJob {
    public enum Status {DONE, RUNNING, QUEUED, STOPPED, FAILED}
    @Getter
    @Setter
    private boolean stopRequested;

    @Getter
    private Status status;
    @Getter
    @Setter
    private String title;
    @Getter
    @Setter
    private String statusText;
    @Getter
    @Setter
    private String log;

    protected void stopped() {
        this.status = Status.STOPPED;
    }

    public abstract void execute() throws InterruptedException;

}
