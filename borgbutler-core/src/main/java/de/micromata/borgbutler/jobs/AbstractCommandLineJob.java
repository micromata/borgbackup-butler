package de.micromata.borgbutler.jobs;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.exec.*;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * A queue is important because Borg doesn't support parallel calls for one repository.
 * For each repository one single queue is allocated.
 */
public abstract class AbstractCommandLineJob extends AbstractJob<String> {
    private Logger log = LoggerFactory.getLogger(AbstractCommandLineJob.class);
    private ExecuteWatchdog watchdog;
    @Getter
    private boolean executeStarted;
    private CommandLine commandLine;
    /**
     * The command line as string. This property is also used as ID for detecting multiple borg calls.
     */
    private String commandLineAsString;
    @Setter
    private File workingDirectory;
    @Setter
    private String description;

    protected abstract CommandLine buildCommandLine();

    @Override
    public Object getId() {
        return getCommandLineAsString();
    }

    public String getCommandLineAsString() {
        if (commandLine == null) {
            commandLine = buildCommandLine();
        }
        if (commandLineAsString == null) {
            commandLineAsString = commandLine.getExecutable() + " " + StringUtils.join(commandLine.getArguments(), " ");
        }
        return commandLineAsString;
    }

    @Override
    public String execute() {
        getCommandLineAsString();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();
        DefaultExecutor executor = new DefaultExecutor();
        if (workingDirectory != null) {
            executor.setWorkingDirectory(workingDirectory);
        }
        //executor.setExitValue(2);
        this.watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
        executor.setWatchdog(watchdog);
        //  ExecuteResultHandler handler = new DefaultExecuteResultHandler();
        PumpStreamHandler streamHandler = new PumpStreamHandler(new LogOutputStream() {
            @Override
            protected void processLine(String line, int level) {
                //log.info(line);
                try {
                    outputStream.write(line.getBytes());
                    outputStream.write("\n".getBytes());
                } catch (IOException ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
        }, new LogOutputStream() {
            @Override
            protected void processLine(String line, int logLevel) {
                //log.error(line);
                try {
                    errorOutputStream.write(line.getBytes());
                    errorOutputStream.write("\n".getBytes());
                } catch (IOException ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
        });
        executor.setStreamHandler(streamHandler);

        if (StringUtils.isNotBlank(this.description)) {
            log.info(description);
        }
        log.info("Executing '" + commandLineAsString + "'...");
        this.executeStarted = true;
        try {
            executor.execute(commandLine, getEnvironment());
            afterSuccess();
        } catch (Exception ex) {
            failed();
            afterFailure(ex);
        }
        return outputStream.toString(Charset.forName("UTF-8"));
    }

    @Override
    protected void cancelRunningProcess() {
        if (watchdog != null) {
            log.info("Cancelling job: " + getId());
            watchdog.destroyProcess();
            watchdog = null;
            setCancelled();
        }
    }

    protected void afterSuccess() {
    }

    protected void afterFailure(Exception ex) {
    }

    protected Map<String, String> getEnvironment() throws IOException {
        return null;
    }

    protected void addEnvironmentVariable(Map<String, String> env, String name, String value) {
        if (StringUtils.isNotBlank(value)) {
            EnvironmentUtils.addVariableToEnvironment(env, name + "=" + value);
        }
    }
}
