package de.micromata.borgbutler.server.rest;

import de.micromata.borgbutler.BorgCommands;
import de.micromata.borgbutler.DiffTool;
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
import org.apache.commons.lang3.StringUtils;
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
import java.nio.file.Files;
import java.util.List;

@Path("/archives")
public class ArchivesRest {
    private static Logger log = LoggerFactory.getLogger(ArchivesRest.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    /**
     *
     * @param repo Name of repository ({@link Repository#getName()}.
     * @param archiveId Id or name of archive.
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @return Repository (including list of archives) as json string.
     * @see JsonUtils#toJson(Object, boolean)
     */
    public String getArchive(@QueryParam("repo") String repoName,
                             @QueryParam("archiveId") String archiveId, @QueryParam("force") boolean force,
                             @QueryParam("prettyPrinter") boolean prettyPrinter) {
        Archive archive = ButlerCache.getInstance().getArchive(repoName, archiveId, force);
        return JsonUtils.toJson(archive, prettyPrinter);
    }

    @GET
    @Path("filelist")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     *
     * @param archiveId Id or name of archive.
     * @param searchString The string to search for (key words separated by white chars, trailing ! char represents exclude).
     * @param mode Flat (default) or tree.
     * @param currentDirectory The current displayed directory (only files and directories contained will be returned).
     * @param maxResultSize maximum number of file items to return (default is 50).
     * @param diffArchiveId If given, the differences between archiveId and diffArchiveId will be returned.
     * @param force If false (default), non cached file lists will not be loaded by borg.
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @return Repository (including list of archives) as json string.
     * @see JsonUtils#toJson(Object, boolean)
     */
    public String getArchiveFileList(@QueryParam("archiveId") String archiveId,
                                     @QueryParam("searchString") String searchString,
                                     @QueryParam("mode") String mode,
                                     @QueryParam("currentDirectory") String currentDirectory,
                                     @QueryParam("maxResultSize") String maxResultSize,
                                     @QueryParam("diffArchiveId") String diffArchiveId,
                                     @QueryParam("force") boolean force,
                                     @QueryParam("prettyPrinter") boolean prettyPrinter) {
        int maxSize = NumberUtils.toInt(maxResultSize, 50);
        FileSystemFilter filter = new FileSystemFilter()
                .setSearchString(searchString)
                .setMaxResultSize(maxSize)
                .setMode(mode)
                .setCurrentDirectory(currentDirectory);
        List<BorgFilesystemItem> items = null;
        if (StringUtils.isBlank(diffArchiveId)) {
            // Get file list (without running diff).
            items = ButlerCache.getInstance().getArchiveContent(archiveId, force,
                    filter);
            if (items == null) {
                return "[{\"mode\": \"notLoaded\"}]";
            }
        } else {
            filter.setMode(FileSystemFilter.Mode.FLAT).setMaxResultSize(-1);
            items = ButlerCache.getInstance().getArchiveContent(archiveId, true, filter);
            List<BorgFilesystemItem> diffItems = ButlerCache.getInstance().getArchiveContent(diffArchiveId, true,
                    filter);
            items = DiffTool.extractDifferences(items, diffItems);
            filter.setMaxResultSize(maxSize)
                    .setMode(mode);
            items = filter.reduce(items);
        }
        return JsonUtils.toJson(items, prettyPrinter);
    }

    @GET
    @Path("/restore")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    /**
     * @param archiveId
     * @param fileNumber The fileNumber of the file or directory in the archive served by BorgButler's
     */
    public Response restore(@QueryParam("archiveId") String archiveId, @QueryParam("fileNumber") int fileNumber) {
        log.info("Requesting file #" + fileNumber + " of archive '" + archiveId + "'.");
        FileSystemFilter filter = new FileSystemFilter().setFileNumber(fileNumber);
        List<BorgFilesystemItem> items = ButlerCache.getInstance().getArchiveContent(archiveId, false,
                filter);
        if (CollectionUtils.isEmpty(items)) {
            log.error("Requested file #" + fileNumber + " not found in archive '" + archiveId
                    + ". (May-be the archive content isn't yet loaded to the cache.");
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            return builder.build();
        }
        if (items.size() != 1) {
            log.error("Requested file #" + fileNumber + " found multiple times (" + items.size() + ") in archive '" + archiveId
                    + "! Please remove the archive files (may-be corrupted).");
            Response.ResponseBuilder builder = Response.status(404);
            return builder.build();
        }
        Archive archive = ButlerCache.getInstance().getArchive(archiveId);
        if (archive == null) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            return builder.build();
        }
        BorgRepoConfig repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(archive.getRepoId());
        try {
            BorgFilesystemItem item = items.get(0);
            File restoreHomeDir = ConfigurationHandler.getConfiguration().getRestoreHomeDir();
            File restoreDir = BorgCommands.extractFiles(restoreHomeDir, repoConfig, archive, item.getPath());
            List<java.nio.file.Path> files = DirUtils.listFiles(restoreDir.toPath());
            if (CollectionUtils.isEmpty(files)) {
                log.error("No files extracted.");
                Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
                return builder.build();
            }
            openFileBrowser(new File(restoreDir, item.getPath()));
            Response.ResponseBuilder builder = Response.status(Response.Status.ACCEPTED);
            return builder.build();
        } catch (IOException ex) {
            log.error("No file extracted: " + ex.getMessage(), ex);
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            return builder.build();
        }
    }

    private void openFileBrowser(File fileDirectory) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
            File file = fileDirectory;
            if (!fileDirectory.exists() || Files.isSymbolicLink(fileDirectory.toPath())) {
                // Open parent.
                file = fileDirectory.getParentFile();
            }
            Desktop.getDesktop().browseFileDirectory(file);
        }
    }

    private Response handleRestoredFiles(BorgRepoConfig repoConfig, Archive archive) {
        // Todo: Handle download of single files as well as download of zip archive (if BorgButler runs remote).
        return null;
       /* File file = path.toFile();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            FileUtils.copyFile(file, baos);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        BorgFilesystemItem item = items.get(0);
        file = new File(item.getPath());
        byte[] byteArray = baos.toByteArray();//result.getAsByteArrayOutputStream().toByteArray();
        Response.ResponseBuilder builder = Response.ok(byteArray);
        builder.header("Content-Disposition", "attachment; filename=" + file.getName());
        // Needed to get the Content-Disposition by client:
        builder.header("Access-Control-Expose-Headers", "Content-Disposition");
        Response response = builder.build();
        return response;

        try {
            //java.nio.file.Path tempDirWithPrefix = Files.createTempDirectory("borgbutler-extract-");
            File restoreHomeDir = ConfigurationHandler.getConfiguration().getRestoreHomeDir();
            File restoreDir = BorgCommands.extractFiles(restoreHomeDir, repoConfig, archive.getName(), item.getPath());
            openFileBrowser(restoreDir);
            List<java.nio.file.Path> files = DirUtils.listFiles(tempDir);
            if (CollectionUtils.isEmpty(files)) {
                log.error("No file extracted.");
                Response.ResponseBuilder builder = Response.status(404);
                return builder.build();
            }
            path = files.get(0);
        } catch (IOException ex) {
            log.error("No file extracted: " + ex.getMessage(), ex);
            Response.ResponseBuilder builder = Response.status(404);
            return builder.build();
        } finally {
           if (tempDir != null) {
                try {
                    FileUtils.deleteDirectory(tempDir.toFile());
                } catch (IOException ex) {
                    log.error("Error while trying to delete temporary directory '" + tempDir.toString() + "': " + ex.getMessage(), ex);
                }
            }
        }*/
    }
}
