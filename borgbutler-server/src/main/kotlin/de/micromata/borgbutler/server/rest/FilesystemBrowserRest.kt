package de.micromata.borgbutler.server.rest

import de.micromata.borgbutler.json.JsonUtils
import de.micromata.borgbutler.server.RunningMode
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.awt.Color
import java.awt.FileDialog
import java.io.File
import javax.servlet.http.HttpServletRequest
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.SwingConstants

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/rest/files")
class FilesystemBrowserRest {

    /**
     * Opens a directory browser or file browser on the desktop app and returns the chosen dir/file. Works only if Browser and Desktop app are running
     * on the same host.
     *
     * @param current The current path of file. If not given the directory/file browser starts with the last used directory or user.home.
     * @return The chosen directory path (absolute path).
     */
    @GetMapping("/browse-local-filesystem")
    fun browseLocalFilesystem(
        request: HttpServletRequest,
        @RequestParam("current", required = false) current: String?
    ): String {
        val msg = RestUtils.checkLocalDesktopAvailable(request)
        if (msg != null) {
            log.info(msg)
            return msg
        }
        if (fileDialog != null || fileChooser != null) {
            log.warn("Cannot call already opened file choose twice. Close file chooser first.")
            return "{\"directory\": \"\"}"
        }
        var file: File? = null
        synchronized(FilesystemBrowserRest::class.java) {
            if (frame == null) {
                val fr = JFrame("BorgButler")
                frame = fr
                fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
                fr.setSize(300, 100)
                fr.setResizable(false)
                fr.setLocationRelativeTo(null)
                fr.setBackground(Color.WHITE)
                fr.getContentPane().setBackground(Color.WHITE)
                val label = JLabel("Click for choosing directory...", SwingConstants.CENTER)
                fr.add(label)
            }
            if (RunningMode.getOSType() == RunningMode.OSType.MAC_OS) {
                // The JFileChooser will hang after several calls, use AWT file dialog instead for Mac OS:
                System.setProperty("apple.awt.fileDialogForDirectories", "true")
                frame?.let {
                    it.setAlwaysOnTop(true)
                    it.setVisible(true)
                }
                try {
                    val dialog =
                        FileDialog(frame, "Choose a directory", FileDialog.LOAD)
                    fileDialog = dialog
                    if (StringUtils.isNotBlank(current)) {
                        dialog.setDirectory(current)
                    }
                    dialog.toFront()
                    dialog.setVisible(true)
                    val filename: String? = dialog.getFile()
                    val directory: String? = dialog.getDirectory()
                    dialog.setVisible(false)
                    if (filename == null) {
                        return ""
                    }
                    file = File(directory, filename)
                    if (file?.isDirectory != true) {
                        file = File(directory)
                    }
                } finally {
                    fileDialog = null
                }
            } else {
                try {
                    val chooser = if (StringUtils.isNotBlank(current)) {
                        JFileChooser(current)
                    } else {
                        JFileChooser()
                    }
                    fileChooser = chooser
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
                    frame?.let {
                        it.setVisible(true)
                        it.setAlwaysOnTop(true)
                    }
                    val returnCode: Int = chooser.showDialog(
                        frame,
                        "Choose"
                    )
                    frame?.let {
                        it.setVisible(false)
                        it.setAlwaysOnTop(false)
                    }
                    if (returnCode == JFileChooser.APPROVE_OPTION) {
                        file = chooser.getSelectedFile()
                    }
                } finally {
                    fileChooser = null
                }
            }
        }
        val filename = if (file != null) JsonUtils.toJson(file!!.absolutePath) else ""
        return "{\"directory\":\"$filename\"}"
    }

    /**
     * @return OK, if the local desktop services such as open file browser etc. are available.
     */
    @GetMapping("/local-fileservices-available")
    fun browseLocalFilesystem(request: HttpServletRequest): String {
        val msg = RestUtils.checkLocalDesktopAvailable(request)
        if (msg != null) {
            log.info(msg)
            return msg
        }
        return "OK"
    }

    companion object {
        private var frame: JFrame? = null
        private var fileDialog: FileDialog? = null
        private var fileChooser: JFileChooser? = null
    }
}
