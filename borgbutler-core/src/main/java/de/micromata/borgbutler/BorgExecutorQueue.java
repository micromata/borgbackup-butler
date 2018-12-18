package de.micromata.borgbutler;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.Configuration;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.config.Definitions;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BorgExecutorQueue {
    private Logger log = LoggerFactory.getLogger(BorgExecutorQueue.class);
    private static BorgExecutorQueue instance = new BorgExecutorQueue();

    public static BorgExecutorQueue getInstance() {
        return instance;
    }

    private ConcurrentLinkedQueue<BorgCommand> commandQueue = new ConcurrentLinkedQueue<>();

    public String execute(BorgCommand command) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        execute(outputStream, command);
        String json = outputStream.toString(Definitions.STD_CHARSET);
        return json;
    }

    public void execute(OutputStream outputStream, BorgCommand command) {
        CommandLine cmdLine = new CommandLine(ConfigurationHandler.getConfiguration().getBorgCommand());
        cmdLine.addArgument(command.command);
        if (command.params != null) {
            for (String param : command.params) {
                if (param != null)
                    cmdLine.addArgument(param);
            }
        }
        cmdLine.addArgument(command.getRepoArchive());
        if (command.args != null) {
            for (String arg : command.args) {
                if (arg != null)
                    cmdLine.addArgument(arg);
            }
        }
        DefaultExecutor executor = new DefaultExecutor();
        if (command.workingDir != null) {
            executor.setWorkingDirectory(command.workingDir);
        }
        //executor.setExitValue(2);
        //ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
        //executor.setWatchdog(watchdog);
        //  ExecuteResultHandler handler = new DefaultExecuteResultHandler();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);
        String borgCall = cmdLine.getExecutable() + " " + StringUtils.join(cmdLine.getArguments(), " ");
        log.info("Executing '" + borgCall + "'...");
        try {
            executor.execute(cmdLine, getEnvironment(command.repoConfig));
        } catch (Exception ex) {
            log.error("Error while creating environment for borg call '" + borgCall + "': " + ex.getMessage(), ex);
            log.error("Response: " + StringUtils.abbreviate(outputStream.toString(), 10000));
            return;
        }
    }


    private Map<String, String> getEnvironment(BorgRepoConfig repoConfig) throws IOException {
        Configuration config = ConfigurationHandler.getConfiguration();
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
