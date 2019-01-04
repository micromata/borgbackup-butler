package de.micromata.borgbutler.server.rest;

import de.micromata.borgbutler.BorgJob;
import de.micromata.borgbutler.BorgQueueExecutor;
import de.micromata.borgbutler.json.JsonUtils;
import de.micromata.borgbutler.server.rest.queue.JsonJob;
import de.micromata.borgbutler.server.rest.queue.JsonJobQueue;
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    /**
     *
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @return Job queues as json string.
     * @see JsonUtils#toJson(Object, boolean)
     */
    public String getJobs(@QueryParam("prettyPrinter") boolean prettyPrinter) {
        BorgQueueExecutor borgQueueExecutor = BorgQueueExecutor.getInstance();
        List<JsonJobQueue> queueList = new ArrayList<>();
        for (String repo : borgQueueExecutor.getRepos()) {
            List<BorgJob<?>> borgJobList = borgQueueExecutor.getJobListCopy(repo);
            JsonJobQueue queue = new JsonJobQueue()
                    .setRepo(repo);
            queueList.add(queue);
            queue.setJobs(new ArrayList<>(borgJobList.size()));
            for (BorgJob<?> borgJob : borgJobList) {
                JsonJob job = new JsonJob(borgJob);
                queue.getJobs().add(job);
            }
        }
        return JsonUtils.toJson(queueList, prettyPrinter);
    }
}
