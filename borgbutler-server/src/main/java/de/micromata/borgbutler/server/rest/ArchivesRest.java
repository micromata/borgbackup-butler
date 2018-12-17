package de.micromata.borgbutler.server.rest;

import de.micromata.borgbutler.BorgCommands;
import de.micromata.borgbutler.cache.ButlerCache;
import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.data.Archive;
import de.micromata.borgbutler.data.FileSystemFilter;
import de.micromata.borgbutler.data.Repository;
import de.micromata.borgbutler.json.JsonUtils;
import de.micromata.borgbutler.json.borg.BorgFilesystemItem;
import de.micromata.borgbutler.utils.DirUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.File;
import java.io.IOException;
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
        log.info("Requesting file #" + fileNumber + " of archive '" + archiveId + "'.");
        FileSystemFilter filter = new FileSystemFilter().setFileNumber(fileNumber);
        List<BorgFilesystemItem> items = ButlerCache.getInstance().getArchiveContent(archiveId, false,
                filter);
        if (CollectionUtils.isEmpty(items)) {
            log.error("Requested file #" + fileNumber + " not found in archive '" + archiveId
                    + ". (May-be the archive content isn't yet loaded to the cache.");
            Response.ResponseBuilder builder = Response.status(404);
            return builder.build();
        }
        if (items.size() != 1) {
            log.error("Requested file #" + fileNumber + " found multiple times (" + items.size() + ") in archive '" + archiveId
                    + "! Please remove the archive files (may-be corrupted).");
            Response.ResponseBuilder builder = Response.status(404);
            return builder.build();
        }
        BorgFilesystemItem item = items.get(0);
        Archive archive = ButlerCache.getInstance().getArchive(archiveId);
        if (archive == null) {
            Response.ResponseBuilder builder = Response.status(404);
            return builder.build();
        }
        BorgRepoConfig repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(archive.getRepoId());
        java.nio.file.Path path = null;
        java.nio.file.Path tempDir = null;
        try {
            tempDir = BorgCommands.extractFiles(repoConfig, archive.getName(), item.getPath());
            openFileBrowser(tempDir);
            List<java.nio.file.Path> files = DirUtils.listFiles(tempDir);
            if (CollectionUtils.isEmpty(files)) {
                log.error("No file extracted.");
                Response.ResponseBuilder builder = Response.status(404);
                return builder.build();
            }
            path = files.get(0);
            File file = path.toFile();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                FileUtils.copyFile(file, baos);
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
            file = new File(item.getPath());
            byte[] byteArray = baos.toByteArray();//result.getAsByteArrayOutputStream().toByteArray();
            Response.ResponseBuilder builder = Response.ok(byteArray);
            builder.header("Content-Disposition", "attachment; filename=" + file.getName());
            // Needed to get the Content-Disposition by client:
            builder.header("Access-Control-Expose-Headers", "Content-Disposition");
            Response response = builder.build();
            return response;
        } catch (IOException ex) {
            log.error("No file extracted: " + ex.getMessage(), ex);
            Response.ResponseBuilder builder = Response.status(404);
            return builder.build();
        } finally {
/*            if (tempDir != null) {
                try {
                    FileUtils.deleteDirectory(tempDir.toFile());
                } catch (IOException ex) {
                    log.error("Error while trying to delete temporary directory '" + tempDir.toString() + "': " + ex.getMessage(), ex);
                }
            }*/
        }
    }

    public static void openFileBrowser(java.nio.file.Path path) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
            Desktop.getDesktop().browseFileDirectory(path.toFile());
        }
    }
}
