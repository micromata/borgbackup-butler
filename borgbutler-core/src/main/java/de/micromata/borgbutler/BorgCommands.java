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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class BorgCommands {
    private static Logger log = LoggerFactory.getLogger(BorgCommands.class);

    /**
     * Executes borg info repository.
     *
     * @param repoConfig
     * @return Parsed repo config returned by Borg command (without archives).
     */
    public static Repository info(BorgRepoConfig repoConfig) {
        String json = execute(repoConfig, "info", repoConfig.getRepo(), "--json");
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
     * The given repository will be cloned and archives will be added.
     * The field {@link Repository#getLastModified()} of masterRepository will be updated.
     *
     * @param repoConfig       The repo config associated to the masterRepository. Needed for the borg call.
     * @param masterRepository Repository without archives.
     * @return Parsed repo config returned by Borg command including archives.
     */
    public static Repository list(BorgRepoConfig repoConfig, Repository masterRepository) {
        String json = execute(repoConfig, "list", masterRepository.getName(), "--json");
        if (json == null) {
            log.error("Can't load archives from repo '" + masterRepository.getName() + "'.");
            return null;
        }
        BorgRepoList repoList = JsonUtils.fromJson(BorgRepoList.class, json);
        if (repoList == null) {
            log.error("Can't load archives from repo '" + masterRepository.getName() + "'.");
            return null;
        }
        masterRepository.setLastModified(DateUtils.format(repoList.getRepository().getLastModified()))
                .setLastCacheRefresh(DateUtils.format(LocalDateTime.now()));
        Repository repository = ObjectUtils.clone(masterRepository)
                .addAll(repoList.getArchives());
        if (repository.getArchives() != null) {
            for (BorgArchive archive : repository.getArchives()) {
                // Reformat Borg date strings.
                archive.setStart(DateUtils.format(archive.getStart()));
                archive.setTime(DateUtils.format(archive.getTime()));
            }
        }
        return repository;
    }

    /**
     * Executes borg info repository::archive.
     * The given repository will be cloned and assigned to the returned archive.
     * The field {@link Repository#getLastModified()} of masterRepository will be updated.
     *
     * @param repoConfig       The repo config associated to the masterRepository. Needed for the borg call.
     * @param archiveName      The name of the archive returned by {@link BorgArchive#getArchive()}
     * @param masterRepository Repository without archives.
     * @return Parsed repo config returned by Borg command including archives.
     */
    public static Archive info(BorgRepoConfig repoConfig, String archiveName, Repository masterRepository) {
        String archiveFullname = repoConfig.getRepo() + "::" + archiveName;
        String json = execute(repoConfig, "info", archiveFullname, "--json");
        if (json == null) {
            return null;
        }
        BorgArchiveInfo archiveInfo = JsonUtils.fromJson(BorgArchiveInfo.class, json);
        if (archiveInfo == null) {
            log.error("Archive '" + archiveFullname + "' not found.");
            return null;
        }
        masterRepository.setLastModified(DateUtils.format(archiveInfo.getRepository().getLastModified()))
                .setLastCacheRefresh(DateUtils.format(LocalDateTime.now()));
        Repository repository = ObjectUtils.clone(masterRepository);
        Archive archive = new Archive()
                .setCache(archiveInfo.getCache())
                .setEncryption(archiveInfo.getEncryption())
                .setRepository(repository);
        if (CollectionUtils.isEmpty(archiveInfo.getArchives())) {
            log.error("The returned borg archive contains no archive infos: " + json);
            return null;
        }
        if (archiveInfo.getArchives().size() > 1   ) {
            log.warn("Archive '" + archiveFullname + "' contains more than one archives!? (Using only first.)");
        }
        BorgArchive2 borgArchive2 = archiveInfo.getArchives().get(0);
        archive.setStart(DateUtils.format(borgArchive2.getStart()))
                .setChunkerParams(borgArchive2.getChunkerParams())
                .setCommandLine(borgArchive2.getCommandLine())
                .setComment(borgArchive2.getComment())
                .setStats(borgArchive2.getStats());
        return archive;
    }

    public static List<BorgFilesystemItem> listArchiveContent(BorgRepoConfig repoConfig, String archiveId) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        execute(outputStream, repoConfig, "list", repoConfig.getRepo() + "::" + archiveId,
                "--json-lines");
        String response = outputStream.toString(Definitions.STD_CHARSET);
        List<BorgFilesystemItem> content = new ArrayList<>();
        try (Scanner scanner = new Scanner(response)) {
            while (scanner.hasNextLine()) {
                String json = scanner.nextLine();
                BorgFilesystemItem item = JsonUtils.fromJson(BorgFilesystemItem.class, json);
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
            if (arg != null)
                cmdLine.addArgument(arg);
        }
        cmdLine.addArgument(repoOrArchive);
        DefaultExecutor executor = new DefaultExecutor();
        //executor.setExitValue(2);
        //ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
        //executor.setWatchdog(watchdog);
        //  ExecuteResultHandler handler = new DefaultExecuteResultHandler();
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
