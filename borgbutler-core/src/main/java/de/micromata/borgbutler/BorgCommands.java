package de.micromata.borgbutler;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.data.Archive;
import de.micromata.borgbutler.data.Repository;
import de.micromata.borgbutler.json.JsonUtils;
import de.micromata.borgbutler.json.borg.*;
import de.micromata.borgbutler.utils.DateUtils;
import de.micromata.borgbutler.utils.ReplaceUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Creates and executes  borg commands by calling system's borg application (Borg must be installed).
 */
public class BorgCommands {
    private static Logger log = LoggerFactory.getLogger(BorgCommands.class);

    /**
     * Executes borg info repository.
     *
     * @param repoConfig
     * @return Parsed repo config returned by Borg command (without archives).
     */
    public static Repository info(BorgRepoConfig repoConfig) {
        BorgCommand command = new BorgCommand()
                .setRepoConfig(repoConfig)
                .setCommand("info")
                .setParams("--json")
                .setDescription("Loading info of repo '" + repoConfig.getDisplayName() + "'.");
        execute(command);
        if (command.getResultStatus() != BorgCommand.ResultStatus.OK) {
            return null;
        }
        BorgRepoInfo repoInfo = JsonUtils.fromJson(BorgRepoInfo.class, command.getResponse());
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
        BorgCommand command = new BorgCommand()
                .setRepoConfig(repoConfig)
                .setCommand("list")
                .setParams("--json")
                .setDescription("Loading list of archives of repo '" + repoConfig.getDisplayName() + "'.");
        execute(command);
        if (command.getResultStatus() != BorgCommand.ResultStatus.OK) {
            log.error("Can't load archives from repo '" + repository.getName() + "'.");
            return;
        }
        BorgRepoList repoList = JsonUtils.fromJson(BorgRepoList.class, command.getResponse());
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
        BorgCommand command = new BorgCommand()
                .setRepoConfig(repoConfig)
                .setCommand("info")
                .setArchive(archive.getName())
                .setParams("--json")
                .setDescription("Loading info of archive '" + archive.getName() + "' of repo '" + repoConfig.getDisplayName() + "'.");
        execute(command);
        if (command.getResultStatus() != BorgCommand.ResultStatus.OK) {
            return;
        }
        BorgArchiveInfo archiveInfo = JsonUtils.fromJson(BorgArchiveInfo.class, command.getResponse());
        if (archiveInfo == null) {
            log.error("Archive '" + command.getRepoArchive() + "' not found.");
            return;
        }
        repository.setLastModified(DateUtils.format(archiveInfo.getRepository().getLastModified()))
                .setLastCacheRefresh(DateUtils.format(LocalDateTime.now()));
        archive.setCache(archiveInfo.getCache())
                .setEncryption(archiveInfo.getEncryption());
        if (CollectionUtils.isEmpty(archiveInfo.getArchives())) {
            log.error("The returned borg archive contains no archive infos: " + command.getAbbreviatedResponse());
            return;
        }
        if (archiveInfo.getArchives().size() > 1) {
            log.warn("Archive '" + command.getRepoArchive() + "' contains more than one archives!? (Using only first.)");
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

    public static List<BorgFilesystemItem> listArchiveContent(BorgRepoConfig repoConfig, Archive archive) {
        BorgCommand command = new BorgCommand()
                .setRepoConfig(repoConfig)
                .setCommand("list")
                .setArchive(archive.getName())
                .setParams("--json-lines")
                .setDescription("Loading list of files of archive '" + archive.getName() + "' of repo '" + repoConfig.getDisplayName() + "'.");
        execute(command);
        List<BorgFilesystemItem> content = new ArrayList<>();
        if (command.getResultStatus() != BorgCommand.ResultStatus.OK) {
            return content;
        }
        try (Scanner scanner = new Scanner(command.getResponse())) {
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

    /**
     * Stores the file in a subdirectory named with the repos display name.
     *
     * @param restoreHomeDir
     * @param repoConfig
     * @param archive
     * @param path
     * @return Used sub directory with the restored content.
     * @throws IOException
     */
    public static File extractFiles(File restoreHomeDir, BorgRepoConfig repoConfig, Archive archive, String path) throws IOException {
        File restoreDir = new File(restoreHomeDir, ReplaceUtils.encodeFilename(repoConfig.getDisplayName(), true));
        if (!restoreDir.exists()) {
            restoreDir.mkdirs();
        }
        BorgCommand command = new BorgCommand()
                .setWorkingDir(restoreDir)
                .setRepoConfig(repoConfig)
                .setCommand("extract")
                .setArchive(archive.getName())
                .setArgs(path)
                .setDescription("Extract content of archive '" + archive.getName()
                                + "' of repo '" + repoConfig.getDisplayName() + "': "
                        + path);
        execute(command);
        return restoreDir;
    }

    private static void execute(BorgCommand command) {
        Validate.notNull(command);
        Validate.notNull(command.getRepoConfig());
        BorgExecutorQueue.getQueue(command.getRepoConfig()).execute(command);
    }
}
