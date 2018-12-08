package de.micromata.borgbutler;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.Configuration;
import de.micromata.borgbutler.config.ConfigurationHandler;
import org.apache.commons.exec.*;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

public class BorgCommands {
    private static Logger log = LoggerFactory.getLogger(BorgCommands.class);

    public static String info(BorgRepoConfig repoConfig) {
        try {
            CommandLine cmdLine = new CommandLine(ConfigurationHandler.getConfiguration().getBorgCommand());
            cmdLine.addArgument("info");
            cmdLine.addArgument(repoConfig.getRepo());
            DefaultExecutor executor = new DefaultExecutor();
            //executor.setExitValue(2);
            ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
            executor.setWatchdog(watchdog);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ExecuteResultHandler handler = new DefaultExecuteResultHandler();
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
            executor.setStreamHandler(streamHandler);
            log.info("Executing '" + cmdLine.getExecutable() + " " + StringUtils.join(cmdLine.getArguments(), " ") + "'...");
            executor.execute(cmdLine, getEnvironment(repoConfig));
            return (outputStream.toString(Charset.forName("UTF-8")));
        } catch (IOException ex) {
            log.error("Error while executing borg command: " + ex.getMessage(), ex);
            return null;
        }
    }

    public static Map<String, String> getEnvironment(BorgRepoConfig repoConfig) throws IOException {
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

    private static void addEnvironmentVariable(Map<String, String> env, String name, String value) {
        if (StringUtils.isNotBlank(value)) {
            EnvironmentUtils.addVariableToEnvironment(env, name + "=" + value);
        }
    }
}
