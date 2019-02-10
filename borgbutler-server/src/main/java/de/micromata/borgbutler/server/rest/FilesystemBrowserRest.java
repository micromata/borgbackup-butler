package de.micromata.borgbutler.server.rest;

import de.micromata.borgbutler.json.JsonUtils;
import de.micromata.borgbutler.server.RunningMode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.swing.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.awt.*;
import java.io.File;

@Path("/files")
public class FilesystemBrowserRest {
    private Logger log = LoggerFactory.getLogger(FilesystemBrowserRest.class);

    /**
     * Opens a directory browser or file browser on the desktop app and returns the chosen dir/file. Works only if Browser and Desktop app are running
     * on the same host.
     *
     * @param current The current path of file. If not given the directory/file browser starts with the last used directory or user.home.
     * @return The chosen directory path (absolute path).
     */
    @GET
    @Path("/browse-local-filesystem")
    @Produces(MediaType.APPLICATION_JSON)
    public String browseLocalFilesystem(@Context HttpServletRequest requestContext, @QueryParam("current") String current) {
        String msg = RestUtils.checkLocalDesktopAvailable(requestContext);
        if (msg != null) {
            log.info(msg);
            return msg;
        }
        if (fileDialog != null || fileChooser != null) {
            log.warn("Cannot call already opened file choose twice. Close file chooser first.");
            return "{\"directory\": \"\"}";
        }
        File file = null;
        synchronized (FilesystemBrowserRest.class) {
            if (frame == null) {
                frame = new JFrame("BorgButler");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(300, 100);
                frame.setResizable(false);
                frame.setLocationRelativeTo(null);
                frame.setBackground(Color.WHITE);
                frame.getContentPane().setBackground(Color.WHITE);
                JLabel label = new JLabel("Click for choosing directory...", SwingConstants.CENTER);
                frame.add(label);
            }
            if (RunningMode.getOSType() == RunningMode.OSType.MAC_OS) {
                // The JFileChooser will hang after several calls, use AWT file dialog instead for Mac OS:
                System.setProperty("apple.awt.fileDialogForDirectories", "true");
                frame.setAlwaysOnTop(true);
                frame.setVisible(true);
                try {
                    fileDialog = new FileDialog(frame, "Choose a directory", FileDialog.LOAD);
                    if (StringUtils.isNotBlank(current)) {
                        fileDialog.setDirectory(current);
                    }
                    fileDialog.toFront();
                    fileDialog.setVisible(true);
                    String filename = fileDialog.getFile();
                    String directory = fileDialog.getDirectory();
                    frame.setVisible(false);
                    if (filename == null) {
                        return "";
                    }
                    file = new File(directory, filename);
                    if (!file.isDirectory()) {
                        file = new File(directory);
                    }
                } finally {
                    fileDialog = null;
                }
            } else {
                try {
                    if (StringUtils.isNotBlank(current)) {
                        fileChooser = new JFileChooser(current);
                    } else {
                        fileChooser = new JFileChooser();
                    }
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    frame.setVisible(true);
                    frame.setAlwaysOnTop(true);
                    int returnCode = fileChooser.showDialog(frame, "Choose");
                    frame.setVisible(false);
                    frame.setAlwaysOnTop(false);
                    if (returnCode == JFileChooser.APPROVE_OPTION) {
                        file = fileChooser.getSelectedFile();
                    }
                } finally {
                    fileChooser = null;
                }
            }
        }
        String filename = file != null ? JsonUtils.toJson(file.getAbsolutePath()) : "";
        String result = "{\"directory\":\"" + filename + "\"}";
        return result;
    }

    /**
     * @return OK, if the local desktop services such as open file browser etc. are available.
     */
    @GET
    @Path("/local-fileservices-available")
    @Produces(MediaType.TEXT_PLAIN)
    public String browseLocalFilesystem(@Context HttpServletRequest requestContext) {
        String msg = RestUtils.checkLocalDesktopAvailable(requestContext);
        if (msg != null) {
            log.info(msg);
            return msg;
        }
        return "OK";
    }

    private static JFrame frame;
    private static FileDialog fileDialog;
    private static JFileChooser fileChooser;
}
