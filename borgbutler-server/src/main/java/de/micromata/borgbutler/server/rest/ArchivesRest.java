package de.micromata.borgbutler.server.rest;

import de.micromata.borgbutler.cache.ButlerCache;
import de.micromata.borgbutler.data.Archive;
import de.micromata.borgbutler.data.FileSystemFilter;
import de.micromata.borgbutler.data.Repository;
import de.micromata.borgbutler.json.JsonUtils;
import de.micromata.borgbutler.json.borg.BorgFilesystemItem;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/archives")
public class ArchivesRest {
    private static Logger log = LoggerFactory.getLogger(ArchivesRest.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    /**
     *
     * @param repo Name of repository ({@link Repository#getName()}.
     * @param archive Id or name of archive.
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @return Repository (including list of archives) as json string.
     * @see JsonUtils#toJson(Object, boolean)
     */
    public String getArchive(@QueryParam("repo") String repoName,
                             @QueryParam("archive") String archiveIdOrName, @QueryParam("force") boolean force,
                             @QueryParam("prettyPrinter") boolean prettyPrinter) {
        Archive archive = ButlerCache.getInstance().getArchive(repoName, archiveIdOrName, force);
        return JsonUtils.toJson(archive, prettyPrinter);
    }

    @GET
    @Path("filelist")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     *
     * @param archiveId Id or name of archive.
     * @param forceLoad If false (default), non cached file lists will not be loaded by borg.
     * @param maxResultSize maximum number of file items to return (default is 50).
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @return Repository (including list of archives) as json string.
     * @see JsonUtils#toJson(Object, boolean)
     */
    public String getArchiveFileLIst(@QueryParam("archiveId") String archiveId,
                                     @QueryParam("searchString") String searchString,
                                     @QueryParam("maxResultSize") String maxResultSize,
                                     @QueryParam("force") boolean force,
                                     @QueryParam("prettyPrinter") boolean prettyPrinter) {
        int maxSize = NumberUtils.toInt(maxResultSize, 50);
        FileSystemFilter filter = new FileSystemFilter()
                .setSearchString(searchString)
                .setMaxResultSize(maxSize);
        List<BorgFilesystemItem> items = ButlerCache.getInstance().getArchiveContent(archiveId, force,
                filter);
        if (items == null) {
            return "[{\"mode\": \"notLoaded\"}]";
        }
        return JsonUtils.toJson(items, prettyPrinter);
    }

    @GET
    @Path("/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    /**
     * @param archiveId
     * @param fileNumber The fileNumber of the file in the archive served by BorgButler's
     * {@link #getArchiveFileLIst(String, String, String, boolean, boolean)}
     */
    public Response downloadFilebyPath(@QueryParam("archiveId") String archiveId, @QueryParam("fileNumber") int fileNumber) {
        log.info("Downloading file #" + fileNumber + " of archive '" + archiveId + "'.");
        return null;
/*        byte[] byteArray = result.getAsByteArrayOutputStream().toByteArray();
        Response.ResponseBuilder builder = Response.ok(byteArray);
        builder.header("Content-Disposition", "attachment; filename=" + filename);
        // Needed to get the Content-Disposition by client:
        builder.header("Access-Control-Expose-Headers", "Content-Disposition");
        Response response = builder.build();
        return response;*/
    }
}
