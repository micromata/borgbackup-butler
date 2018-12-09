package de.micromata.borgbutler;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.Configuration;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.config.Definitions;
import de.micromata.borgbutler.json.JsonUtils;
import de.micromata.borgbutler.json.borg.*;
import org.apache.commons.exec.*;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
    public static ArchiveInfo info(BorgRepoConfig repoConfig, String archive) {
        String json = execute(repoConfig, "info", repoConfig.getRepo() + "::" + archive, "--json");
        if (json == null) {
            return null;
        }
        ArchiveInfo archiveList = JsonUtils.fromJson(ArchiveInfo.class, json);
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

    public static List<FilesystemItem> list(BorgRepoConfig repoConfig, Archive archive) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        execute(outputStream, repoConfig, "list", repoConfig.getRepo() + "::" + archive.getArchive(),
                "--json-lines");
        String response = outputStream.toString(Definitions.STD_CHARSET);
        try {
            IOUtils.copy(new StringReader(response), new FileWriter("response.json"));
        }catch (IOException ex) {

        }
        List<FilesystemItem> content = new ArrayList<>();
        try (Scanner scanner = new Scanner(response)) {
            while (scanner.hasNextLine()) {
                String json = scanner.nextLine();
                FilesystemItem item = JsonUtils.fromJson(FilesystemItem.class, json);
                content.add(item);
            }
        }
        return content;
    }

    private static String execute(BorgRepoConfig repoConfig, String command, String repoOrArchive, String... args) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        execute(outputStream, repoConfig, command, repoOrArchive, args);
        String json = outputStream.toString(Definitions.STD_CHARSET);
        return json;
    }

    private static void execute(OutputStream outputStream, BorgRepoConfig repoConfig, String command, String repoOrArchive, String... args) {
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
        ExecuteResultHandler handler = new DefaultExecuteResultHandler();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);
        String borgCall = cmdLine.getExecutable() + " " + StringUtils.join(cmdLine.getArguments(), " ");
        log.info("Executing '" + borgCall + "'...");
        try {
            executor.execute(cmdLine, getEnvironment(repoConfig));
        } catch (Exception ex) {
            log.error("Error while creating environment for borg call '" + borgCall + "': " + ex.getMessage(), ex);
            log.error("Response: " + StringUtils.abbreviate(outputStream.toString(), 10000));
            return;
        }
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
