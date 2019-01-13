package de.micromata.borgbutler.demo;

import de.micromata.borgbutler.BorgCommand;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.config.Definitions;
import de.micromata.borgbutler.data.Repository;
import de.micromata.borgbutler.jobs.JobResult;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DemoRepos {
    private static Logger log = LoggerFactory.getLogger(DemoRepos.class);
    private static final String DEMO_IDENTIFIER = "borgbutler-demo";

    private static final String[] REPOS = {"fast", "slow", "very-slow"};
    private static List<Repository> demoRepos;

    /**
     * If configured by the user, demo repositories are added to the given list. If not configured this method does nothing.
     *
     * @param repositoryList
     */
    public static void addDemoRepos(List<Repository> repositoryList) {
        if (!ConfigurationHandler.getConfiguration().isShowDemoRepos()) {
            return;
        }
        init();
        for (Repository repo : demoRepos) {
            repositoryList.add(repo);
        }
    }

    public static boolean isDemo(String idOrName) {
        return StringUtils.startsWith(idOrName, DEMO_IDENTIFIER);
    }

    public static Repository getRepo(String idOrName) {
        if (!isDemo(idOrName)) {
            log.info("Given idOrName doesn't fit any demo repository: " + idOrName);
            return null;
        }
        init();
        for (Repository repo : demoRepos) {
            if (StringUtils.equals(idOrName, repo.getId())) {
                return repo;
            }
        }
        return null;
    }

    public static BorgRepoConfig getRepoConfig(String idOrName) {
        BorgRepoConfig repoConfig = new BorgRepoConfig();
        Repository repository = getRepo(idOrName);
        if (repository == null) {
            log.info("Given idOrName doesn't fit any demo repository: " + idOrName);
            return null;
        }
        repoConfig.setRepo(repository.getName())
                .setId(repository.getId())
                .setDisplayName(repository.getName());
        return repoConfig;
    }

    public static JobResult<String> execute(BorgCommand command) {
        StringBuilder sb = new StringBuilder();
        if (command.getArchive() != null) {
            sb.append("archive-");
        } else {
            sb.append("repo-");
        }
        sb.append(command.getCommand());
        if (command.getArchive() != null) {
            sb.append("-").append(command.getArchive());
        }
        sb.append(".json.gz");
        String file = sb.toString();
        try (InputStream inputStream = new GzipCompressorInputStream(DemoRepos.class.getResourceAsStream("/demodata/" + file))) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer, Definitions.STD_CHARSET);
            return new JobResult<String>().setResultObject(writer.toString()).setStatus(JobResult.Status.OK);
        } catch (IOException ex) {
            log.error("Error while reading demo file '" + file + "': " + ex.getMessage() + ".");
            return null;
        }
    }

    private static void init() {
        demoRepos = new ArrayList<>();
        demoRepos.add(new Repository()
                .setId(DEMO_IDENTIFIER + "-fast")
                .setName(DEMO_IDENTIFIER + "-fast")
                .setDisplayName("Demo repository fast"));
        demoRepos.add(new Repository()
                .setId(DEMO_IDENTIFIER + "-slow")
                .setName(DEMO_IDENTIFIER + "-slow")
                .setDisplayName("Demo repository slow"));
        demoRepos.add(new Repository()
                .setId(DEMO_IDENTIFIER + "-very-slow")
                .setName(DEMO_IDENTIFIER + "-very-slow")
                .setDisplayName("Demo repository very-slow"));

    }
}
