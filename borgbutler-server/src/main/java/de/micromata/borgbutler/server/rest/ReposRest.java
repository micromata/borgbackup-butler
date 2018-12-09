package de.micromata.borgbutler.server.rest;

import de.micromata.borgbutler.cache.ButlerCache;
import de.micromata.borgbutler.json.JsonUtils;
import de.micromata.borgbutler.json.borg.Repository;
import org.apache.commons.collections4.CollectionUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/repos")
public class ReposRest {
    @GET
    @Path("refresh")
    @Produces(MediaType.TEXT_PLAIN)
    /**
     * Reloads all templates on the server.
     * @return "OK"
     */
    public String refresh() {
        ButlerCache.getInstance().getRepoInfoCache().clearAndReset();
        return "OK";
    }

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
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @see JsonUtils#toJson(Object, boolean)
     */
    public String getList(@QueryParam("prettyPrinter") boolean prettyPrinter) {
        List<Repository> repositories = ButlerCache.getInstance().getAllRepositories();
        if (CollectionUtils.isEmpty(repositories)) {
            return "";
        }
        return JsonUtils.toJson(repositories, prettyPrinter);
    }
}
