package de.micromata.borgbutler;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.jobs.AbstractCommandLineJob;
import lombok.Getter;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * A queue is important because Borg doesn't support parallel calls for one repository.
 * For each repository one single queue is allocated.
 */
public class BorgJob<T> extends AbstractCommandLineJob<String> {
    private Logger log = LoggerFactory.getLogger(BorgJob.class);
    @Getter
    private BorgCommand command;
    @Getter
    protected T payload;

    public BorgJob(BorgCommand command) {
        this.command = command;
        setWorkingDirectory(command.getWorkingDir());
        setDescription(command.getDescription());
    }

    @Override
    protected CommandLine buildCommandLine() {
        CommandLine commandLine = new CommandLine(ConfigurationHandler.getConfiguration().getBorgCommand());
        commandLine.addArgument(command.getCommand());
        if (command.getParams() != null) {
            for (String param : command.getParams()) {
                if (param != null)
                    commandLine.addArgument(param);
            }
        }
        if (command.getRepoArchive() != null) {
            commandLine.addArgument(command.getRepoArchive());
        }
        if (command.getArgs() != null) {
            for (String arg : command.getArgs()) {
                if (arg != null)
                    commandLine.addArgument(arg);
            }
        }
        return commandLine;
    }

    @Override
    protected Map<String, String> getEnvironment() throws IOException {
        BorgRepoConfig repoConfig = command.getRepoConfig();
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
}
