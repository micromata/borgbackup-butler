package de.micromata.borgbutler;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.config.Definitions;
import org.apache.commons.exec.*;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A queue is important because Borg doesn't support parallel calls for one repository.
 * For each repository one single queue is allocated.
 */
public class BorgExecutorQueue {
    private Logger log = LoggerFactory.getLogger(BorgExecutorQueue.class);
    // key is the repo name.
    private static Map<String, BorgExecutorQueue> queueMap = new HashMap<>();

    public static BorgExecutorQueue getQueue(BorgRepoConfig config) {
        synchronized (queueMap) {
            String queueName = config != null ? config.getRepo() : "--NO_REPO--";
            BorgExecutorQueue queue = queueMap.get(queueName);
            if (queue == null) {
                queue = new BorgExecutorQueue();
                queueMap.put(queueName, queue);
            }
            return queue;
        }
    }

    //private ConcurrentLinkedQueue<BorgCommand> commandQueue = new ConcurrentLinkedQueue<>();

    public void execute(BorgCommand command) {
        synchronized (this) {
            _execute(command);
        }
    }

    private void _execute(BorgCommand command) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();
        CommandLine cmdLine = new CommandLine(ConfigurationHandler.getConfiguration().getBorgCommand());
        cmdLine.addArgument(command.getCommand());
        if (command.getParams() != null) {
            for (String param : command.getParams()) {
                if (param != null)
                    cmdLine.addArgument(param);
            }
        }
        if (command.getRepoArchive() != null) {
            cmdLine.addArgument(command.getRepoArchive());
        }
        if (command.getArgs() != null) {
            for (String arg : command.getArgs()) {
                if (arg != null)
                    cmdLine.addArgument(arg);
            }
        }
        DefaultExecutor executor = new DefaultExecutor();
        if (command.getWorkingDir() != null) {
            executor.setWorkingDirectory(command.getWorkingDir());
        }
        //executor.setExitValue(2);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
        executor.setWatchdog(watchdog);
        //  ExecuteResultHandler handler = new DefaultExecuteResultHandler();
        PumpStreamHandler streamHandler = new PumpStreamHandler(new LogOutputStream() {
            @Override
            protected void processLine(String line, int level) {
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
                try {
                    errorOutputStream.write(line.getBytes());
                    errorOutputStream.write("\n".getBytes());
                } catch (IOException ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
        });
        executor.setStreamHandler(streamHandler);
        String borgCall = cmdLine.getExecutable() + " " + StringUtils.join(cmdLine.getArguments(), " ");
        if (StringUtils.isNotBlank(command.getDescription())) {
            log.info(command.getDescription());
        }
        log.info("Executing '" + borgCall + "'...");
        try {
            executor.execute(cmdLine, getEnvironment(command.getRepoConfig()));
            command.setResultStatus(BorgCommand.ResultStatus.OK);
        } catch (Exception ex) {
            log.error("Error while creating environment for borg call '" + borgCall + "': " + ex.getMessage(), ex);
            command.setResultStatus(BorgCommand.ResultStatus.ERROR);
        }
        command.setResponse(outputStream.toString(Definitions.STD_CHARSET));
        if (command.getResultStatus() == BorgCommand.ResultStatus.ERROR) {
            log.error("Response: " + command.getAbbreviatedResponse());
        }
    }


    private Map<String, String> getEnvironment(BorgRepoConfig repoConfig) throws IOException {
        if (repoConfig == null) {
            return null;
        }
        Map<String, String> env = EnvironmentUtils.getProcEnvironment();
        addEnvironmentVariable(env, "BORG_REPO", repoConfig.getRepo());
        addEnvironmentVariable(env, "BORG_RSH", repoConfig.getRsh());
        addEnvironmentVariable(env, "BORG_PASSPHRASE", repoConfig.getPassphrase());
        String passcommand = repoConfig.getPasswordCommand();
        if (StringUtils.isNotBlank(passcommand)) {
            // For MacOS BORG_PASSCOMMAND="security find-generic-password -a $USER -s borg-passphrase -w"
            passcommand = passcommand.replace("$USER", System.getProperty("user.name"));
            addEnvironmentVariable(env, "BORG_PASSCOMMAND", passcommand);
        }
        return env;
    }

    private void addEnvironmentVariable(Map<String, String> env, String name, String value) {
        if (StringUtils.isNotBlank(value)) {
            EnvironmentUtils.addVariableToEnvironment(env, name + "=" + value);
        }
    }

    private BorgExecutorQueue() {

    }
}
