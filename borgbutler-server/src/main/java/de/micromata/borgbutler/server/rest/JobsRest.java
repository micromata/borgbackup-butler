package de.micromata.borgbutler.server.rest;

import de.micromata.borgbutler.BorgJob;
import de.micromata.borgbutler.BorgQueueExecutor;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.jobs.AbstractJob;
import de.micromata.borgbutler.json.JsonUtils;
import de.micromata.borgbutler.json.borg.ProgressInfo;
import de.micromata.borgbutler.server.rest.queue.JsonJob;
import de.micromata.borgbutler.server.rest.queue.JsonJobQueue;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/jobs")
public class JobsRest {
    private static Logger log = LoggerFactory.getLogger(JobsRest.class);

    private static List<JsonJobQueue> testList;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * @param repo If given, only the job queue of the given repo will be returned.
     * @param testMode If true, then a test job list is created.
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @return Job queues as json string.
     * @see JsonUtils#toJson(Object, boolean)
     */
    public String getJobs(@QueryParam("repo") String repo,
                          @QueryParam("testMode") boolean testMode,
                          @QueryParam("oldJobs") boolean oldJobs,
                          @QueryParam("prettyPrinter") boolean prettyPrinter) {
        if (testMode) {
            // Return dynamic test queue:
            return returnTestList(prettyPrinter);
        }
        boolean validRepo = false;
        if (StringUtils.isNotBlank(repo) && !"null".equals(repo) && !"undefined".equals(repo)) {
            validRepo = true;
        }
        BorgQueueExecutor borgQueueExecutor = BorgQueueExecutor.getInstance();
        List<JsonJobQueue> queueList = new ArrayList<>();
        if (validRepo) { // Get only the queue of the given repo:
            JsonJobQueue queue = getQueue(repo, oldJobs);
            if (queue != null) {
                queueList.add(queue);
            }
        } else { // Get all the queues (of all repos).
            for (String rep : borgQueueExecutor.getRepos()) {
                JsonJobQueue queue = getQueue(rep, oldJobs);
                if (queue != null) {
                    queueList.add(queue);
                }
            }
        }
        return JsonUtils.toJson(queueList, prettyPrinter);
    }

    private JsonJobQueue getQueue(String repo, boolean oldJobs) {
        BorgQueueExecutor borgQueueExecutor = BorgQueueExecutor.getInstance();
        BorgRepoConfig repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(repo);
        if (repoConfig == null) {
            return null;
        }
        List<BorgJob<?>> borgJobList = borgQueueExecutor.getJobListCopy(repoConfig, oldJobs);
        if (CollectionUtils.isEmpty(borgJobList))
            return null;
        JsonJobQueue queue = new JsonJobQueue().setRepo(repoConfig.getDisplayName());
        queue.setJobs(new ArrayList<>(borgJobList.size()));
        for (BorgJob<?> borgJob : borgJobList) {
            JsonJob job = new JsonJob(borgJob);
            queue.getJobs().add(job);
        }
        return queue;
    }

    @Path("/cancel")
    @GET
    /**
     * @param uniqueJobNumberString The id of the job to cancel.
     */
    public void cancelJob(@QueryParam("uniqueJobNumber") String uniqueJobNumberString) {
        Long uniqueJobNumber = null;
        try {
            uniqueJobNumber = Long.parseLong(uniqueJobNumberString);
        } catch (NumberFormatException ex) {
            log.error("Can't cancel job, because unique job number couln't be parsed (long value expected): " + uniqueJobNumberString);
            return;
        }
        BorgQueueExecutor.getInstance().cancelJob(uniqueJobNumber);
    }

    /**
     * Only for test purposes and development.
     *
     * @param prettyPrinter
     * @return
     */
    private String returnTestList(boolean prettyPrinter) {
        if (testList == null) {
            testList = new ArrayList<>();
            long uniqueJobNumber = 100000;
            JsonJobQueue queue = new JsonJobQueue().setRepo("My Computer");
            addTestJob(queue, "info", "my-macbook", 0, 2342)
                    .setUniqueJobNumber(uniqueJobNumber++);
            addTestJob(queue, "list", "my-macbook", -1, -1)
                    .setUniqueJobNumber(uniqueJobNumber++);
            testList.add(queue);

            queue = new JsonJobQueue().setRepo("My Server");
            addTestJob(queue, "list", "my-server", 0, 1135821)
                    .setUniqueJobNumber(uniqueJobNumber++);
            addTestJob(queue, "info", "my-server", -1, -1)
                    .setUniqueJobNumber(uniqueJobNumber++);
            testList.add(queue);
        } else {
            for (JsonJobQueue jobQueue : testList) {
                for (JsonJob job : jobQueue.getJobs()) {
                    if (job.getStatus() != AbstractJob.Status.RUNNING) continue;
                    long current = job.getProgressInfo().getCurrent();
                    long total = job.getProgressInfo().getTotal();
                    if (StringUtils.startsWith(job.getProgressInfo().getMessage(), "Calculating")) {
                        // Info is a faster operation:
                        current += Math.random() * total / 5;
                    } else {
                        // than get the complete archive file list:
                        current += Math.random() * total / 30;
                    }
                    if (current > total) {
                        current = 0; // Reset to beginning.
                    }
                    job.getProgressInfo().setCurrent(current);
                    if (job.getProgressText().startsWith("Calculating")) {
                        job.getProgressInfo().setMessage("Calculating statistics...  " + Math.round(100 * current / total) + "%");
                    }
                    job.buildProgressText();
                }
            }
        }
        return JsonUtils.toJson(testList, prettyPrinter);
    }

    /**
     * Only for test purposes and development.
     *
     * @param queue
     * @param operation
     * @param host
     * @param current
     * @param total
     * @return
     */
    private JsonJob addTestJob(JsonJobQueue queue, String operation, String host, long current, long total) {
        ProgressInfo progressInfo = new ProgressInfo()
                .setCurrent(current)
                .setTotal(total);
        JsonJob job = new JsonJob()
                .setProgressInfo(progressInfo)
                .setStatus(AbstractJob.Status.QUEUED);
        if ("info".equals(operation)) {
            progressInfo.setMessage("Calculating statistics... ");
            job.setDescription("Loading info of archive '" + host + "-2018-12-05T23:10:33' of repo '" + queue.getRepo() + "'.")
                    .setCommandLineAsString("borg info --json --log-json --progress ssh://...:23/./backups/" + host + "::" + host + "-2018-12-05T23:10:33");
        } else {
            progressInfo.setMessage("Getting file list... ");
            job.setDescription("Loading list of files of archive '" + host + "-2018-12-05T17:30:38' of repo '" + queue.getRepo() + "'.")
                    .setCommandLineAsString("borg list --json-lines ssh://...:23/./backups/" + host + "::" + host + "-2018-12-05T17:30:38");
        }
        job.buildProgressText();
        if (current >= 0) {
            job.setStatus(AbstractJob.Status.RUNNING);
        } else {
            job.setStatus(AbstractJob.Status.QUEUED);
        }
        if (queue.getJobs() == null) {
            queue.setJobs(new ArrayList<>());
        }
        queue.getJobs().add(job);
        return job;
    }
}
