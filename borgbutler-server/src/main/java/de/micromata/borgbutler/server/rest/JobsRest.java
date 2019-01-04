package de.micromata.borgbutler.server.rest;

import de.micromata.borgbutler.BorgJob;
import de.micromata.borgbutler.BorgQueueExecutor;
import de.micromata.borgbutler.data.Archive;
import de.micromata.borgbutler.data.Repository;
import de.micromata.borgbutler.json.JsonUtils;
import de.micromata.borgbutler.server.rest.queue.JsonJob;
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
     * @param repo Name of repository ({@link Repository#getName()}.
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @return Job queue as json string.
     * @see JsonUtils#toJson(Object, boolean)
     */
    public String getJobs(@QueryParam("repo") String repoName,
                          @QueryParam("prettyPrinter") boolean prettyPrinter) {
        BorgQueueExecutor borgQueueExecutor = BorgQueueExecutor.getInstance();
        List<BorgJob<?>> borgJobList = borgQueueExecutor.getJobListCopy(repoName);
        List<JsonJob> jobList = new ArrayList<>(borgJobList.size());
        for (BorgJob<?> borgJob : borgJobList) {
            JsonJob job = new JsonJob();
            job.setTitle(borgJob.getTitle());
            // job.setProgressText(borgJob.get);
            job.setStatus(borgJob.getStatus());
            job.setCancelledRequested(borgJob.isCancelledRequested());
        }
        Archive archive = null;
        return JsonUtils.toJson(archive, prettyPrinter);
    }
}
