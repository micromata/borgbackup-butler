package de.micromata.borgbutler;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.config.Definitions;
import de.micromata.borgbutler.jobs.AbstractCommandLineJob;
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
public class BorgJob extends AbstractCommandLineJob {
    private Logger log = LoggerFactory.getLogger(BorgJob.class);
    private BorgCommand command;

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
    protected void afterSuccess() {
        command.setResultStatus(BorgCommand.ResultStatus.OK);
        command.setResponse(outputStream.toString(Definitions.STD_CHARSET));
    }

    @Override
    protected void afterFailure(Exception ex) {
        command.setResultStatus(BorgCommand.ResultStatus.ERROR);
        command.setResponse(outputStream.toString(Definitions.STD_CHARSET));
        log.error("Response: " + command.getAbbreviatedResponse());
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
