package de.micromata.borgbutler.server.rest;

import de.micromata.borgbutler.cache.ButlerCache;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.data.Repository;
import de.micromata.borgbutler.json.JsonUtils;
import de.micromata.borgbutler.json.borg.BorgRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/repos")
public class ReposRest {
    private static Logger log = LoggerFactory.getLogger(ReposRest.class);

    /**
     *
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @return A list of repositories of type {@link BorgRepository}.
     * @see JsonUtils#toJson(Object, boolean)
     */
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public String getList(@QueryParam("prettyPrinter") boolean prettyPrinter) {
        List<Repository> repositories = ButlerCache.getInstance().getAllRepositories();
        if (CollectionUtils.isEmpty(repositories)) {
            return "[]";
        }
        return JsonUtils.toJson(repositories, prettyPrinter);
    }

    /**
     *
     * @param id id or name of repo.
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @return {@link Repository} (without list of archives) as json string.
     * @see JsonUtils#toJson(Object, boolean)
     */
    @GET
    @Path("repo")
    @Produces(MediaType.APPLICATION_JSON)
    public String getRepo(@QueryParam("id") String id, @QueryParam("prettyPrinter") boolean prettyPrinter) {
        Repository repository = ButlerCache.getInstance().getRepository(id);
        return JsonUtils.toJson(repository, prettyPrinter);
    }

    /**
     *
     * @param id id or name of repo.
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @return {@link BorgRepoConfig} as json string.
     * @see JsonUtils#toJson(Object, boolean)
     */
    @GET
    @Path("repoConfig")
    @Produces(MediaType.APPLICATION_JSON)
    public String getRepoConfig(@QueryParam("id") String id, @QueryParam("prettyPrinter") boolean prettyPrinter) {
        BorgRepoConfig repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(id);
        return JsonUtils.toJson(repoConfig, prettyPrinter);
    }

    @POST
    @Path("repoConfig")
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


    /**
     *
     * @param id id or name of repo.
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @return {@link Repository} (including list of archives) as json string.
     * @see JsonUtils#toJson(Object, boolean)
     */
    @GET
    @Path("repoArchiveList")
    @Produces(MediaType.APPLICATION_JSON)
    public String getRepoArchiveList(@QueryParam("id") String id, @QueryParam("force") boolean force,
                                     @QueryParam("prettyPrinter") boolean prettyPrinter) {
        if (force) {
            Repository repo = ButlerCache.getInstance().getRepository(id);
            ButlerCache.getInstance().clearRepoCacheAccess(repo);
        }
        Repository repository = ButlerCache.getInstance().getRepositoryArchives(id);
        return JsonUtils.toJson(repository, prettyPrinter);
    }
}
