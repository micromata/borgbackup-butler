package de.micromata.borgbutler.jobs;

import org.apache.commons.exec.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class TestJob extends AbstractCommandLineJob {
    private Logger log = LoggerFactory.getLogger(TestJob.class);
    private int time;
    private File counterScript;
    private int failOn = -1;

    TestJob(int time, File counterScript) {
        this(time, -1, counterScript);
    }

    TestJob(int time, int failOn, File counterScript) {
        this.time = time;
        this.failOn = failOn;
        this.counterScript = counterScript;
    }

    @Override
    public Object getId() {
        return time;
    }

    @Override
    protected CommandLine buildCommandLine() {
        CommandLine commandLine = new CommandLine(counterScript.getAbsolutePath());
        commandLine.addArgument(String.valueOf(this.time));
        commandLine.addArgument(String.valueOf(this.failOn));
        return commandLine;
    }

    @Override
    protected void afterFailure(Exception ex) {
        if (failOn < 0 && getStatus() != Status.CANCELLED) {
            log.error("Error while executing script '" + getCommandLineAsString() + "': " + ex.getMessage(), ex);
        }
    }
}
