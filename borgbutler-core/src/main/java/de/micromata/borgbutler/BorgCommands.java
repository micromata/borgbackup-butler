package de.micromata.borgbutler;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.data.Archive;
import de.micromata.borgbutler.data.Repository;
import de.micromata.borgbutler.jobs.JobResult;
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
import java.util.List;

/**
 * Creates and executes  borg commands by calling system's borg application (Borg must be installed).
 */
public class BorgCommands {
    private static Logger log = LoggerFactory.getLogger(BorgCommands.class);

    /**
     * Executes borg --version
     *
     * @return version string.
     */
    public static String version() {
        BorgCommand command = new BorgCommand()
                .setParams("--version")
                .setDescription("Getting borg version.");
        JobResult<String> jobResult = getResult(command);
        if (jobResult == null || jobResult.getStatus() != JobResult.Status.OK) {
            return null;
        }
        String version = jobResult.getResultObject();
        log.info("Borg version: " + version);
        return version;
    }

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
                .setParams("--json") // --progress has no effect.
                .setDescription("Loading info of repo '" + repoConfig.getDisplayName() + "'.");
        JobResult<String> jobResult = getResult(command);
        if (jobResult == null || jobResult.getStatus() != JobResult.Status.OK) {
            return null;
        }
        String result = jobResult.getResultObject();
        BorgRepoInfo repoInfo = JsonUtils.fromJson(BorgRepoInfo.class, result);
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
                .setParams("--json") // --progress has no effect.
                .setDescription("Loading list of archives of repo '" + repoConfig.getDisplayName() + "'.");
        JobResult<String> jobResult = getResult(command);
        if (jobResult == null || jobResult.getStatus() != JobResult.Status.OK) {
            log.error("Can't load archives from repo '" + repository.getName() + "'.");
            return;
        }
        String result = jobResult.getResultObject();
        BorgRepoList repoList = JsonUtils.fromJson(BorgRepoList.class, result);
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
                .setParams("--json", "--log-json", "--progress")
                .setDescription("Loading info of archive '" + archive.getName() + "' of repo '" + repoConfig.getDisplayName() + "'.");
        JobResult<String> jobResult = getResult(command);
        if (jobResult == null || jobResult.getStatus() != JobResult.Status.OK) {
            return;
        }
        String result = jobResult.getResultObject();
        BorgArchiveInfo archiveInfo = JsonUtils.fromJson(BorgArchiveInfo.class, result);
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
                .setDuration(borgArchive.getDuration());
    }

    public static List<BorgFilesystemItem> listArchiveContent(BorgRepoConfig repoConfig, Archive archive) {
        BorgCommand command = new BorgCommand()
                .setRepoConfig(repoConfig)
                .setCommand("list")
                .setArchive(archive.getName())
                .setParams("--json-lines")
                .setDescription("Loading list of files of archive '" + archive.getName() + "' of repo '" + repoConfig.getDisplayName() + "'.");
        // The returned job might be an already queued or running one!
        final ProgressInfo progressInfo = new ProgressInfo()
                .setMessage("Getting file list...")
                .setCurrent(0)
                .setTotal(archive.getStats().getNfiles());
        BorgJob<List<BorgFilesystemItem>> job = BorgQueueExecutor.getInstance().execute(new BorgJob<List<BorgFilesystemItem>>(command) {
            @Override
            protected void processStdOutLine(String line, int level) {
                BorgFilesystemItem item = JsonUtils.fromJson(BorgFilesystemItem.class, line);
                item.setMtime(DateUtils.format(item.getMtime()));
                payload.add(item);
                if ("-".equals(item.getType())) {
                    // Only increment for files, because number of files is the total.
                    setProgressInfo(progressInfo.incrementCurrent());
                }
            }
        });
        job.payload = new ArrayList<>();
        JobResult<String> jobResult = job.getResult();
        if (jobResult == null ||jobResult.getStatus() != JobResult.Status.OK) {
            return null;
        }
        List<BorgFilesystemItem> items = job.payload;
        job.cleanUp(); // payload will be released.
        return items;
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
                .setParams("--log-json", "--progress")
                .setArchive(archive.getName())
                .setArgs(path)
                .setDescription("Extract content of archive '" + archive.getName()
                        + "' of repo '" + repoConfig.getDisplayName() + "': "
                        + path);
        JobResult<String> jobResult = getResult(command);
        return restoreDir;
    }

    private static JobResult<String> getResult(BorgCommand command) {
        BorgJob<Void> job = execute(command);
        JobResult<String> jobResult = job.getResult();
        job.cleanUp();
        return jobResult;
    }

    private static BorgJob<Void> execute(BorgCommand command) {
        Validate.notNull(command);
        return BorgQueueExecutor.getInstance().execute(command);
    }
}
