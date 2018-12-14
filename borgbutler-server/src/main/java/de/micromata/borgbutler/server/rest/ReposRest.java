package de.micromata.borgbutler.server.rest;

import de.micromata.borgbutler.cache.ButlerCache;
import de.micromata.borgbutler.data.Repository;
import de.micromata.borgbutler.json.JsonUtils;
import de.micromata.borgbutler.json.borg.BorgRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/repos")
public class ReposRest {
    private static Logger log = LoggerFactory.getLogger(ReposRest.class);

    @GET
    @Path("repo")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     *
     * @param id
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @see JsonUtils#toJson(Object, boolean)
     */
    public String getTemplate(@QueryParam("id") String id, @QueryParam("prettyPrinter") boolean prettyPrinter) {
        Repository repository = ButlerCache.getInstance().getRepository(id);
        return JsonUtils.toJson(repository, prettyPrinter);
    }

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     *
     * @param force If true, a reload of all repositories is forced.
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @return A list of repositories of type {@link BorgRepository}.
     * @see JsonUtils#toJson(Object, boolean)
     */
    public String getList(@QueryParam("force") boolean force, @QueryParam("prettyPrinter") boolean prettyPrinter) {
        if (force) {
            ButlerCache.getInstance().clearRepoCacheAccess();
        }
        List<Repository> repositories = ButlerCache.getInstance().getAllRepositories();
        if (CollectionUtils.isEmpty(repositories)) {
            return "";
        }
        return JsonUtils.toJson(repositories, prettyPrinter);
    }
}
