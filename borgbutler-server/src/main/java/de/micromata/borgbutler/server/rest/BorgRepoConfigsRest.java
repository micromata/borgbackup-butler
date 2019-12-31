package de.micromata.borgbutler.server.rest;

import de.micromata.borgbutler.BorgCommandResult;
import de.micromata.borgbutler.BorgCommands;
import de.micromata.borgbutler.cache.ButlerCache;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.data.Repository;
import de.micromata.borgbutler.jobs.JobResult;
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
        if (newRepoConfig == null) {
            log.error("Internal Rest error. Can't parse BorgRepoConfig: " + jsonConfig);
            return;
        }
        if ("new".equals(newRepoConfig.getId())) {
            newRepoConfig.setId(null);
            ConfigurationHandler.getConfiguration().add(newRepoConfig);
        } else if ("init".equals(newRepoConfig.getId())) {
            newRepoConfig.setId(null);
            ConfigurationHandler.getConfiguration().add(newRepoConfig);
        } else {
            BorgRepoConfig repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(newRepoConfig.getId());
            if (repoConfig == null) {
                log.error("Can't find repo config '" + newRepoConfig.getId() + "'. Can't save new settings.");
                return;
            }
            ButlerCache.getInstance().clearRepoCacheAccess(repoConfig.getRepo());
            ButlerCache.getInstance().clearRepoCacheAccess(newRepoConfig.getRepo());
            repoConfig.copyFrom(newRepoConfig);
        }
        ConfigurationHandler.getInstance().save();
    }

    /**
     * @param idOrName id or name of repo to remove from BorgButler.
     * @return "OK" if removed or error string.
     */
    @GET
    @Path("remove")
    @Produces(MediaType.APPLICATION_JSON)
    public String removeRepoConfig(@QueryParam("id") String idOrName) {
        boolean result = ConfigurationHandler.getConfiguration().remove(idOrName);
        if (!result) {
            String error = "Repo config with id or name '" + idOrName + "' not found. Can't remove the repo.";
            log.error(error);
            return error;
        }
        return "OK";
    }

    /**
     * @param jsonRepoConfig All configuration value of the repo to check.
     * @return Result of borg (tbd.).
     */
    @POST
    @Path("check")
    @Produces(MediaType.APPLICATION_JSON)
    public String checkConfig(String jsonRepoConfig) {
        log.info("Testing repo config: " + jsonRepoConfig);
        BorgRepoConfig repoConfig = JsonUtils.fromJson(BorgRepoConfig.class, jsonRepoConfig);
        BorgCommandResult<Repository> result = BorgCommands.info(repoConfig);
        return result.getStatus() == JobResult.Status.OK ? "OK" : result.getError();
    }
}
