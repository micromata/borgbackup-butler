package de.micromata.borgbutler.demo;

import de.micromata.borgbutler.BorgCommand;
import de.micromata.borgbutler.BorgJob;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.config.Definitions;
import de.micromata.borgbutler.data.Repository;
import de.micromata.borgbutler.jobs.JobResult;
import de.micromata.borgbutler.json.JsonUtils;
import de.micromata.borgbutler.json.borg.ProgressInfo;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class DemoRepos {
    private enum Type {FAST, SLOW, VERY_SLOW}

    private static Logger log = LoggerFactory.getLogger(DemoRepos.class);
    private static final String DEMO_IDENTIFIER = "borgbutler-demo";

    private static final String[] REPOS = {"fast", "slow", "very-slow"};
    private static List<BorgRepoConfig> demoRepos;

    /**
     * If configured by the user, demo repositories are added to the given list. If not configured this method does nothing.
     *
     * @param repositoryList
     * @return repo list including demo repos if configured. If not configured, the given list is returned (no op).
     */
    public static List<BorgRepoConfig> getAllRepos(List<BorgRepoConfig> repositoryList) {
        if (!ConfigurationHandler.getConfiguration().getShowDemoRepos()) {
            return repositoryList;
        }
        init(repositoryList);
        List<BorgRepoConfig> list = new ArrayList<>();
        list.addAll(repositoryList);
        list.addAll(demoRepos);
        return list;
    }

    public static boolean isDemo(String name) {
        return StringUtils.startsWith(name, DEMO_IDENTIFIER);
    }

    public static void repoWasRead(BorgRepoConfig repoConfig, Repository repository) {
        if (!isDemo(repository.getName())) {
            return;
        }
        repository.setId(repository.getId() + "-" + REPOS[getType(repoConfig).ordinal()]);
    }

    public static JobResult<String> execute(BorgJob job) {
        BorgCommand command = job.getCommand();
        if (!StringUtils.equalsAny(command.getCommand(), "list", "info")) {
            log.info("Commmand '" + command.getCommand() + "' not supported for demo repositories.");
            return new JobResult<String>().setStatus(JobResult.Status.ERROR);
        }
        StringBuilder sb = new StringBuilder();
        boolean archive = command.getArchive() != null;
        if (archive) {
            sb.append("archive-");
        } else {
            sb.append("repo-");
        }
        sb.append(command.getCommand());
        if (archive) {
            sb.append("-").append(command.getArchive());
        }
        sb.append(".json.gz");
        int wait = 0;
        Type type = getType(command.getRepoConfig());
        if (type == Type.VERY_SLOW) {
            wait = 10;
        } else if (type == Type.SLOW) {
            wait = 1;
        }
        String file = sb.toString();
        log.info("Loading demo archive from '" + file + "'...");
        try (InputStream inputStream = new GzipCompressorInputStream(DemoRepos.class.getResourceAsStream("/demodata/" + file))) {
            if (wait > 0) {
                ProgressInfo progress = new ProgressInfo()
                        .setMessage("Faked demo progress")
                        .setTotal(10 * wait);
                for (int i = 0; i < 10 * wait; i++) {
                    if (job.isCancellationRequested()) {
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        // Do nothing.
                    }
                    job.processStdErrLine(JsonUtils.toJson(progress.setCurrent(i)), 0);
                }
            }
            if (archive && "list".equals(command.getCommand())) {
                try (Scanner scanner = new Scanner(inputStream)) {
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        job.processStdOutLine(line, 0);
                    }
                    return new JobResult<String>().setStatus(JobResult.Status.OK);
                }
            } else {
                StringWriter writer = new StringWriter();
                IOUtils.copy(inputStream, writer, Definitions.STD_CHARSET);
                return new JobResult<String>().setResultObject(writer.toString()).setStatus(JobResult.Status.OK);
            }
        } catch (IOException ex) {
            log.error("Error while reading demo file '" + file + "': " + ex.getMessage() + ".");
            return null;
        }
    }

    private static Type getType(BorgRepoConfig repoConfig) {
        if (repoConfig.getRepo().endsWith("very-slow")) {
            return Type.VERY_SLOW;
        } else if (repoConfig.getRepo().endsWith("slow")) {
            return Type.SLOW;
        }
        return Type.FAST;
    }

    private static void init(List<BorgRepoConfig> repositoryList) {
        synchronized (DEMO_IDENTIFIER) {
            if (demoRepos != null) {
                return;
            }
            synchronized (repositoryList) {
                // Remove demo repo entries if persisted in former config files:
                Iterator<BorgRepoConfig> it = repositoryList.iterator();
                while (it.hasNext()) {
                    BorgRepoConfig repoConfig = it.next();
                    if (isDemo(repoConfig.getRepo())) {
                        it.remove();
                    }
                }
            }
            demoRepos = new ArrayList<>();
            BorgRepoConfig config = new BorgRepoConfig();
            config.setRepo(DEMO_IDENTIFIER + "-fast");
            config.setDisplayName("Demo repository fast");
            demoRepos.add(config);

            config = new BorgRepoConfig();
            config.setRepo(DEMO_IDENTIFIER + "-slow");
            config.setDisplayName("Demo repository slow");
            demoRepos.add(config);

            config = new BorgRepoConfig();
            config.setRepo(DEMO_IDENTIFIER + "-very-slow");
            config.setDisplayName("Demo repository very-slow");
            demoRepos.add(config);
        }
    }
}
