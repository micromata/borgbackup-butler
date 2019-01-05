package de.micromata.borgbutler.server.rest;

import de.micromata.borgbutler.BorgJob;
import de.micromata.borgbutler.BorgQueueExecutor;
import de.micromata.borgbutler.cache.ButlerCache;
import de.micromata.borgbutler.data.Repository;
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
     * @param testMode If true, then a test job list is created.
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @return Job queues as json string.
     * @see JsonUtils#toJson(Object, boolean)
     */
    public String getJobs(@QueryParam("testMode") boolean testMode, @QueryParam("prettyPrinter") boolean prettyPrinter) {
        if (testMode) {
            return returnTestList(prettyPrinter);
        }
        BorgQueueExecutor borgQueueExecutor = BorgQueueExecutor.getInstance();
        List<JsonJobQueue> queueList = new ArrayList<>();
        for (String repo : borgQueueExecutor.getRepos()) {
            Repository repository = ButlerCache.getInstance().getRepositoryArchives(repo);
            String title = repository != null ? repository.getDisplayName() : repo;
            List<BorgJob<?>> borgJobList = borgQueueExecutor.getJobListCopy(repo);
            if (CollectionUtils.isEmpty(borgJobList))
                continue;
            JsonJobQueue queue = new JsonJobQueue()
                    .setRepo(title);
            queueList.add(queue);
            queue.setJobs(new ArrayList<>(borgJobList.size()));
            for (BorgJob<?> borgJob : borgJobList) {
                JsonJob job = new JsonJob(borgJob);
                queue.getJobs().add(job);
            }
        }
        return JsonUtils.toJson(queueList, prettyPrinter);
    }

    private String returnTestList(boolean prettyPrinter) {
        if (testList == null) {
            testList = new ArrayList<>();
            JsonJobQueue queue = new JsonJobQueue().setRepo("My Computer");
            addTestJob(queue, "info", "my-macbook", 0, 2342);
            addTestJob(queue, "list", "my-macbook", -1, -1);
            testList.add(queue);

            queue = new JsonJobQueue().setRepo("My Server");
            addTestJob(queue, "list", "my-server", 0, 1135821);
            addTestJob(queue, "info", "my-server", -1, -1);
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
