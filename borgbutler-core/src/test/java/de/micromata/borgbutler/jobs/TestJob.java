package de.micromata.borgbutler.jobs;

import org.apache.commons.exec.*;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class TestJob extends AbstractJob {
    private Logger log = LoggerFactory.getLogger(TestJob.class);
    private int time;
    private File counterScript;

    TestJob(int time, File counterScript) {
        this.time = time;
        this.counterScript = counterScript;
    }

    @Override
    public void execute() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CommandLine cmdLine = new CommandLine(counterScript.getAbsolutePath());
        cmdLine.addArgument(String.valueOf(this.time));
        DefaultExecutor executor = new DefaultExecutor();
        ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
        executor.setWatchdog(watchdog);
        PumpStreamHandler streamHandler = new PumpStreamHandler(new LogOutputStream() {
            @Override
            protected void processLine(String line, int level) {
                log.info(line);
                try {
                    outputStream.write(line.getBytes());
                    outputStream.write("\n".getBytes());
                } catch (IOException ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
        });
        executor.setStreamHandler(streamHandler);
        log.info("Executing '" + counterScript.getAbsolutePath() + " " + this.time + "'...");
        try {
            executor.execute(cmdLine);
        } catch (Exception ex) {
            log.error("Error while executing script: " + ex.getMessage(), ex);
        }
        log.info(outputStream.toString(Charset.forName("UTF-8")));
    }
}
