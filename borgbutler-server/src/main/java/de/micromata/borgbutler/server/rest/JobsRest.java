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
            addTestJob(queue, "Calculating statistics... ",
                    "Loading info of archive 'my-computer-2018-12-05T23:10:33' of repo 'My-Computer-Cloud'.", 0, 1000);
            addTestJob(queue, null,
                    "Loading list of files of archive 'my-computer-2018-12-05T23:10:33' of repo 'My-Computer-Cloud'.", 0, 0);
            testList.add(queue);

            queue = new JsonJobQueue().setRepo("My Server");
            addTestJob(queue, "Getting file list...",
                    "Loading list of files of archive 'my-server-2018-12-05T23:10:33' of repo 'My-Server-Cloud'.", 0, 0);
            addTestJob(queue, null,
                    "Loading info of archive 'my-server-2018-12-05T23:10:33' of repo 'My-Server-Cloud'.", 0, 1000);
            testList.add(queue);
        } else {
            for (JsonJobQueue jobQueue : testList) {
                for (JsonJob job : jobQueue.getJobs()) {
                    if (job.getStatus() != AbstractJob.Status.RUNNING) continue;
                    if (job.getProgressText().startsWith("Calculating")) {
                        long current = job.getProgressInfo().getCurrent();
                        current += Math.random() * 100;
                        if (current > 1000) {
                            current = 0; // Reset to beginning.
                        }
                        job.getProgressInfo().setCurrent(current);
                        job.getProgressInfo().setMessage("Calculating statistics...  " + Math.round(current / 10) + "%");
                    } else {
                        long current = job.getProgressInfo().getCurrent();
                        current += Math.random() * 10000;
                        job.getProgressInfo().setCurrent(current);
                    }
                    job.buildProgressText();
                }
            }
        }
        return JsonUtils.toJson(testList, prettyPrinter);
    }


    private JsonJob addTestJob(JsonJobQueue queue, String message, String description, long current, long total) {
        ProgressInfo msg = new ProgressInfo()
                .setMessage(message)
                .setCurrent(current)
                .setTotal(total);
        JsonJob job = new JsonJob()
                .setProgressInfo(msg)
                .setDescription(description)
                .setStatus(AbstractJob.Status.QUEUED)
                .setCommandLineAsString(description);
        job.buildProgressText();
        if (message != null) {
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
