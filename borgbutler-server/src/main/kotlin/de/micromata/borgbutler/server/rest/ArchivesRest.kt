package de.micromata.borgbutler.server.rest

import de.micromata.borgbutler.BorgCommands
import de.micromata.borgbutler.cache.ButlerCache
import de.micromata.borgbutler.config.BorgRepoConfig
import de.micromata.borgbutler.config.ConfigurationHandler
import de.micromata.borgbutler.data.Archive
import de.micromata.borgbutler.data.DiffFileSystemFilter
import de.micromata.borgbutler.data.FileSystemFilter
import de.micromata.borgbutler.json.JsonUtils
import de.micromata.borgbutler.json.borg.BorgFilesystemItem
import de.micromata.borgbutler.utils.DirUtils
import mu.KotlinLogging
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.apache.coyote.Response
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/rest/archives")
class ArchivesRest {
    /**
     * @param repoName      Name of repository ([Repository.getName].
     * @param archiveId     Id or name of archive.
     * @return Repository (including list of archives) as json string.
     * @see JsonUtils.toJson
     */
    @GetMapping
    fun getArchive(
        @RequestParam("repo") repoName: String,
        @RequestParam("archiveId") archiveId: String,
        @RequestParam("force", required = false) force: Boolean?    ): Archive? {
        val archive = ButlerCache.getInstance().getArchive(repoName, archiveId, force == true)
        if (force == true) {
            ButlerCache.getInstance().deleteCachedArchiveContent(repoName, archiveId)
        }
        return archive
    }

    /**
     * @param archiveId                     Id or name of archive.
     * @param searchString                  The string to search for (key words separated by white chars, trailing ! char represents exclude).
     * @param mode                          Flat (default) or tree.
     * @param currentDirectory              The current displayed directory (only files and directories contained will be returned).
     * @param maxResultSize                 maximum number of file items to return (default is 50).
     * @param diffArchiveId                 If given, the differences between archiveId and diffArchiveId will be returned.
     * @param autoChangeDirectoryToLeafItem If given, this method will step automatically into single sub directories.
     * @param force                         If false (default), non cached file lists will not be loaded by borg.
     * @return Repository (including list of archives) as json string or [{"mode": "notLoaded"}] if no file list loaded.
     * @see JsonUtils.toJson
     */
    @GetMapping("filelist")
    fun getArchiveFileList(
        @RequestParam("archiveId") archiveId: String,
        @RequestParam("searchString", required = false) searchString: String?,
        @RequestParam("mode", required = false) mode: String?,
        @RequestParam("currentDirectory", required = false) currentDirectory: String?,
        @RequestParam("maxResultSize", required = false) maxResultSize: String?,
        @RequestParam("diffArchiveId", required = false) diffArchiveId: String?,
        @RequestParam("autoChangeDirectoryToLeafItem", required = false) autoChangeDirectoryToLeafItem: Boolean?,
        @RequestParam("force", required = false) force: Boolean?
    ): String {
        val diffMode = StringUtils.isNotBlank(diffArchiveId)
        val maxSize = NumberUtils.toInt(maxResultSize, 50)
        val filter = if (diffMode) DiffFileSystemFilter() else FileSystemFilter()
        filter.setSearchString(searchString)
            .setCurrentDirectory(currentDirectory)
            .setAutoChangeDirectoryToLeafItem(autoChangeDirectoryToLeafItem == true)
        var items: List<BorgFilesystemItem>?
        if (diffMode) {
            filter.setMode(FileSystemFilter.Mode.FLAT)
            items = ButlerCache.getInstance().getArchiveContent(archiveId, true, filter)
            val diffItems: List<BorgFilesystemItem> = ButlerCache.getInstance().getArchiveContent(
                diffArchiveId, true,
                filter
            )
            filter.setMaxResultSize(maxSize)
                .setMode(mode)
            items = (filter as DiffFileSystemFilter).extractDifferences(items, diffItems)
            items = filter.reduce(items)
        } else {
            filter.setMode(mode)
                .setMaxResultSize(maxSize)
            // Get file list (without running diff).
            items = ButlerCache.getInstance().getArchiveContent(
                archiveId, force == true,
                filter
            )
            if (items == null) {
                return "[{\"mode\": \"notLoaded\"}]"
            }
        }
        return JsonUtils.toJson(items)
    }

    /**
     * @param archiveId
     * @param openDownloads
     * @param fileNumber    The fileNumber of the file or directory in the archive served by BorgButler's
     */
    @GetMapping("/restore")
    fun restore(
        @RequestParam("archiveId") archiveId: String,
        @RequestParam("openDownloads", required = false) openDownloads: Boolean?,
        @RequestParam("fileNumber") fileNumber: Int?
    ): ResponseEntity<*> {
        log.info("Requesting file #$fileNumber of archive '$archiveId'.")
        val filter: FileSystemFilter = FileSystemFilter().setFileNumber(fileNumber)
        val items: List<BorgFilesystemItem> = ButlerCache.getInstance().getArchiveContent(
            archiveId, false,
            filter
        )
        if (CollectionUtils.isEmpty(items)) {
            log.error(
                "Requested file #" + fileNumber + " not found in archive '" + archiveId
                        + ". (May-be the archive content isn't yet loaded to the cache."
            )
            return RestUtils.notFound()
        }
        if (items.size != 1) {
            log.error(
                "Requested file #" + fileNumber + " found multiple times (" + items.size + ") in archive '" + archiveId
                        + "! Please remove the archive files (may-be corrupted)."
            )
            return RestUtils.notFound()
        }
        val archive: Archive = ButlerCache.getInstance().getArchive(archiveId) ?: return RestUtils.notFound()

        val repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(archive.repoId)
        try {
            val item: BorgFilesystemItem = items[0]
            val restoreHomeDir: File = ConfigurationHandler.getConfiguration().getRestoreHomeDir()
            val restoreDir: File = BorgCommands.extractFiles(restoreHomeDir, repoConfig, archive, item.getPath())
            val files: List<Path?> = DirUtils.listFiles(restoreDir.toPath())
            if (CollectionUtils.isEmpty(files)) {
                log.error("No files extracted.")
                return RestUtils.notFound()
            }
            if (openDownloads == true) openFileBrowser(File(restoreDir, item.getPath()))
            return ResponseEntity.ok("OK")
        } catch (ex: IOException) {
            log.error("No file extracted: " + ex.message, ex)
            return RestUtils.notFound()
        }
    }

    private fun openFileBrowser(fileDirectory: File) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
            var file: File? = fileDirectory
            if (!fileDirectory.exists() || Files.isSymbolicLink(fileDirectory.toPath())) {
                // Open parent.
                file = fileDirectory.parentFile
            }
            Desktop.getDesktop().browseFileDirectory(file)
        }
    }

    private fun handleRestoredFiles(repoConfig: BorgRepoConfig, archive: Archive): Response? {
        // Todo: Handle download of single files as well as download of zip archive (if BorgButler runs remote).
        return null
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
