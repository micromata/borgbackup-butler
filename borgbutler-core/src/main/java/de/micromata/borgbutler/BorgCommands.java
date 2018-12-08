package de.micromata.borgbutler;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.Configuration;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.json.JsonUtils;
import de.micromata.borgbutler.json.borg.Archive1;
import de.micromata.borgbutler.json.borg.ArchiveList;
import de.micromata.borgbutler.json.borg.RepoInfo;
import de.micromata.borgbutler.json.borg.RepoList;
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

    /**
     * Executes borg info repository
     *
     * @param repoConfig
     * @return Parsed repo config returned by Borg command.
     */
    public static RepoInfo info(BorgRepoConfig repoConfig) {
        String json = execute(repoConfig, "info", repoConfig.getRepo(), "--json");
        if (json == null) {
            return null;
        }
        RepoInfo repoInfo = JsonUtils.fromJson(RepoInfo.class, json);
        repoInfo.setOriginalJson(json);
        return repoInfo;
    }

    /**
     * Executes borg info archive
     *
     * @param repoConfig
     * @param archive
     * @return
     */
    public static ArchiveList info(BorgRepoConfig repoConfig, Archive1 archive) {
        String json = execute(repoConfig, "info", repoConfig.getRepo() + "::" + archive.getArchive(), "--json");
        if (json == null) {
            return null;
        }
        ArchiveList archiveList = JsonUtils.fromJson(ArchiveList.class, json);
        archiveList.setOriginalJson(json);
        return archiveList;
    }

    public static RepoList list(BorgRepoConfig repoConfig) {
        String json = execute(repoConfig, "list", repoConfig.getRepo(), "--json");
        if (json == null) {
            return null;
        }
        RepoList repoList = JsonUtils.fromJson(RepoList.class, json);
        repoList.setOriginalJson(json);
        return repoList;
    }

    private static String execute(BorgRepoConfig repoConfig, String command, String repoOrArchive, String... args) {
        CommandLine cmdLine = new CommandLine(ConfigurationHandler.getConfiguration().getBorgCommand());
        cmdLine.addArgument(command);
        for (String arg : args) {
            cmdLine.addArgument(arg);
        }
        cmdLine.addArgument(repoOrArchive);
        DefaultExecutor executor = new DefaultExecutor();
        //executor.setExitValue(2);
        //ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
        //executor.setWatchdog(watchdog);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ExecuteResultHandler handler = new DefaultExecuteResultHandler();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);
        String borgCall = cmdLine.getExecutable() + " " + StringUtils.join(cmdLine.getArguments(), " ");
        log.info("Executing '" + borgCall + "'...");
        try {
            executor.execute(cmdLine, getEnvironment(repoConfig));
        } catch (Exception ex) {
            log.error("Error while creating environment for borg call '" + borgCall + "': " + ex.getMessage(), ex);
            String response = outputStream.toString(Charset.forName("UTF-8"));
            log.error("Response: " + response);
            return null;
        }
        String json = outputStream.toString(Charset.forName("UTF-8"));
        return json;
    }

    private static Map<String, String> getEnvironment(BorgRepoConfig repoConfig) throws IOException {
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
