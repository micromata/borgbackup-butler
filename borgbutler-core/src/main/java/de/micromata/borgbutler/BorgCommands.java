package de.micromata.borgbutler;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.Configuration;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.config.Definitions;
import de.micromata.borgbutler.data.Archive;
import de.micromata.borgbutler.data.Repository;
import de.micromata.borgbutler.json.JsonUtils;
import de.micromata.borgbutler.json.borg.*;
import de.micromata.borgbutler.utils.DateUtils;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

public class BorgCommands {
    private static Logger log = LoggerFactory.getLogger(BorgCommands.class);

    /**
     * Executes borg info repository.
     *
     * @param repoConfig
     * @return Parsed repo config returned by Borg command (without archives).
     */
    public static Repository info(BorgRepoConfig repoConfig) {
        Context context = new Context().setRepoConfig(repoConfig).setCommand("info").setParams("--json");
        String json = execute(context);
        if (json == null) {
            return null;
        }
        BorgRepoInfo repoInfo = JsonUtils.fromJson(BorgRepoInfo.class, json);
        BorgRepository borgRepository = repoInfo.getRepository();
        Repository repository = new Repository()
                .setId(borgRepository.getId())
                .setName(repoConfig.getRepo())
                .setDisplayName(repoConfig.getDisplayName())
                .setLastModified(DateUtils.format(borgRepository.getLastModified()))
                .setLocation(borgRepository.getLocation())
                .setCache(repoInfo.getCache())
                .setEncryption(repoInfo.getEncryption())
                .setSecurityDir(repoInfo.getSecurityDir())
                .setLastCacheRefresh(DateUtils.format(LocalDateTime.now()));
        return repository;
    }

    /**
     * Executes borg list repository.
     * The given repository will be used and archives will be added.
     *
     * @param repoConfig The repo config associated to the masterRepository. Needed for the borg call.
     * @param repository Repository without archives, archives will be loaded.
     */
    public static void list(BorgRepoConfig repoConfig, Repository repository) {
        Context context = new Context().setRepoConfig(repoConfig).setCommand("list").setParams("--json");
        String json = execute(context);
        if (json == null) {
            log.error("Can't load archives from repo '" + repository.getName() + "'.");
            return;
        }
        BorgRepoList repoList = JsonUtils.fromJson(BorgRepoList.class, json);
        if (repoList == null || CollectionUtils.isEmpty(repoList.getArchives())) {
            log.error("Can't load archives from repo '" + repository.getName() + "'.");
            return;
        }
        repository.setLastModified(DateUtils.format(repoList.getRepository().getLastModified()))
                .setLastCacheRefresh(DateUtils.format(LocalDateTime.now()));
        for (BorgArchive borgArchive : repoList.getArchives()) {
            Archive archive = new Archive()
                    .setName(borgArchive.getArchive())
                    .setId(borgArchive.getId())
                    .setStart(DateUtils.format(borgArchive.getStart()))
                    .setTime(DateUtils.format(borgArchive.getTime()))
                    .setRepoId(repository.getId())
                    .setRepoName(repository.getName())
                    .setRepoDisplayName(repoConfig.getDisplayName());
            repository.add(archive);
        }
    }

    /**
     * Executes borg info repository::archive.
     * The given repository will be modified.
     * The field {@link Repository#getLastModified()} of masterRepository will be updated.
     *
     * @param repoConfig The repo config associated to the repository. Needed for the borg call.
     * @param archive    The archive to update.
     * @param repository Repository without archives.
     */
    public static void info(BorgRepoConfig repoConfig, Archive archive, Repository repository) {
        Context context = new Context().setRepoConfig(repoConfig).setCommand("info").setArchive(archive.getName())
                .setParams("--json");
        String json = execute(context);
        if (json == null) {
            return;
        }
        BorgArchiveInfo archiveInfo = JsonUtils.fromJson(BorgArchiveInfo.class, json);
        if (archiveInfo == null) {
            log.error("Archive '" + context.getRepoArchive() + "' not found.");
            return;
        }
        repository.setLastModified(DateUtils.format(archiveInfo.getRepository().getLastModified()))
                .setLastCacheRefresh(DateUtils.format(LocalDateTime.now()));
        archive.setCache(archiveInfo.getCache())
                .setEncryption(archiveInfo.getEncryption());
        if (CollectionUtils.isEmpty(archiveInfo.getArchives())) {
            log.error("The returned borg archive contains no archive infos: " + json);
            return;
        }
        if (archiveInfo.getArchives().size() > 1) {
            log.warn("Archive '" + context.getRepoArchive() + "' contains more than one archives!? (Using only first.)");
        }
        BorgArchive2 borgArchive = archiveInfo.getArchives().get(0);
        archive.setStart(DateUtils.format(borgArchive.getStart()))
                .setChunkerParams(borgArchive.getChunkerParams())
                .setCommandLine(borgArchive.getCommandLine())
                .setComment(borgArchive.getComment())
                .setStats(borgArchive.getStats())
                .setLimits(borgArchive.getLimits())
                .setHostname(borgArchive.getHostname())
                .setUsername(borgArchive.getUsername())
                .setEnd(DateUtils.format(borgArchive.getEnd()))
                .setDuration(borgArchive.getDuration())
        ;
    }

    public static List<BorgFilesystemItem> listArchiveContent(BorgRepoConfig repoConfig, String archive) {
        Context context = new Context().setRepoConfig(repoConfig).setCommand("list").setArchive(archive)
                .setParams("--json-lines");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        execute(outputStream, context);
        String response = outputStream.toString(Definitions.STD_CHARSET);
        List<BorgFilesystemItem> content = new ArrayList<>();
        try (Scanner scanner = new Scanner(response)) {
            while (scanner.hasNextLine()) {
                String json = scanner.nextLine();
                BorgFilesystemItem item = JsonUtils.fromJson(BorgFilesystemItem.class, json);
                item.setMtime(DateUtils.format(item.getMtime()));
                content.add(item);
            }
        }
        Collections.sort(content); // Sort by path.
        return content;
    }

    public static Path extractFiles(BorgRepoConfig repoConfig, String archive, String path) throws IOException {
        Path tempDirWithPrefix = Files.createTempDirectory("borbutler");
        Context context = new Context().setWorkingDir(tempDirWithPrefix.toFile()).setRepoConfig(repoConfig)
                .setCommand("extract").setArchive(archive).setArgs(path);
        execute(context);
        return tempDirWithPrefix;
    }

    private static String execute(Context context) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        execute(outputStream, context);
        String json = outputStream.toString(Definitions.STD_CHARSET);
        return json;
    }

    private static void execute(OutputStream outputStream, Context context) {
        CommandLine cmdLine = new CommandLine(ConfigurationHandler.getConfiguration().getBorgCommand());
        cmdLine.addArgument(context.command);
        if (context.params != null) {
            for (String param : context.params) {
                if (param != null)
                    cmdLine.addArgument(param);
            }
        }
        cmdLine.addArgument(context.getRepoArchive());
        if (context.args != null) {
            for (String arg : context.args) {
                if (arg != null)
                    cmdLine.addArgument(arg);
            }
        }
        DefaultExecutor executor = new DefaultExecutor();
        if (context.workingDir != null) {
            executor.setWorkingDirectory(context.workingDir);
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
            executor.execute(cmdLine, getEnvironment(context.repoConfig));
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

    private static class Context {
        @Setter
        File workingDir;
        String[] args;
        String[] params;
        @Setter
        BorgRepoConfig repoConfig;
        @Setter
        String command;
        @Setter
        String archive;

        Context setArgs(String... args) {
            this.args = args;
            return this;
        }

        Context setParams(String... params) {
            this.params = params;
            return this;
        }

        String getRepoArchive() {
            if (archive == null) {
                return repoConfig.getRepo();
            }
            return repoConfig.getRepo() + "::" + archive;
        }
    }
}
