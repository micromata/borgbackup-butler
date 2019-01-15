package de.micromata.borgbutler.server.rest;

import de.micromata.borgbutler.BorgQueueExecutor;
import de.micromata.borgbutler.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/system")
public class SystemInfoRest {
    private static Logger log = LoggerFactory.getLogger(SystemInfoRest.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("info")
    /**
     * @return The total number of jobs queued or running (and other statistics): {@link de.micromata.borgbutler.BorgQueueStatistics}.
     * @see JsonUtils#toJson(Object, boolean)
     */
    public String getStatistics() {
        SystemInfo systemInfonfo = new SystemInfo()
                .setQueueStatistics(BorgQueueExecutor.getInstance().getStatistics())
                .setConfigurationOK(false);
        return JsonUtils.toJson(systemInfonfo);
    }
}
