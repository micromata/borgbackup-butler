package de.micromata.borgbutler.server.rest;

import de.micromata.borgbutler.cache.ButlerCache;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/repoConfig")
public class BorgRepoConfigsRest {
    private static Logger log = LoggerFactory.getLogger(BorgRepoConfigsRest.class);

    /**
     * @param id            id or name of repo.
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @return {@link BorgRepoConfig} as json string.
     * @see JsonUtils#toJson(Object, boolean)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getRepoConfig(@QueryParam("id") String id, @QueryParam("prettyPrinter") boolean prettyPrinter) {
        BorgRepoConfig repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(id);
        return JsonUtils.toJson(repoConfig, prettyPrinter);
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public void setRepoConfig(String jsonConfig) {
        BorgRepoConfig newRepoConfig = JsonUtils.fromJson(BorgRepoConfig.class, jsonConfig);
        BorgRepoConfig repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(newRepoConfig.getId());
        if (repoConfig == null) {
            log.error("Can't find repo config '" + newRepoConfig.getId() + "'. Can't save new settings.");
            return;
        }
        ButlerCache.getInstance().clearRepoCacheAccess(repoConfig.getRepo());
        ButlerCache.getInstance().clearRepoCacheAccess(newRepoConfig.getRepo());
        repoConfig.copyFrom(newRepoConfig);
        ConfigurationHandler.getInstance().save();
    }
}
