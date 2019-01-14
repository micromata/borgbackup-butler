package de.micromata.borgbutler.server;

import de.micromata.borgbutler.cache.ButlerCache;
import de.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.json.borg.BorgFilesystemItem;
import de.micromata.borgbutler.server.jetty.JettyServer;
import de.micromata.borgbutler.server.user.SingleUserManager;
import de.micromata.borgbutler.server.user.UserManager;
import org.apache.commons.cli.*;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Main {
    private static Logger log = LoggerFactory.getLogger(Main.class);

    private static final Main main = new Main();

    private JettyServer server;
    private boolean shutdownInProgress;

    private Main() {
    }

    public static void main(String[] args) {
        main._start(args);
    }

    public static JettyServer startUp(String... restPackageNames) {
        return main._startUp(restPackageNames);
    }

    public static void shutdown() {
        main._shutdown();
    }


    private void _start(String[] args) {
        ConfigurationHandler.setConfigClazz(ServerConfiguration.class);
        // create Options object
        Options options = new Options();
        options.addOption("e", "extract-archive-content", true, "Extracts the content of an archive cache file only (doesn't start the server). A complete file list of the archive will be extracted to stdout.");
        options.addOption("p", "port", true, "The default port for the web server.");
        options.addOption("q", "quiet", false, "Don't open browser automatically.");
        options.addOption("h", "help", false, "Print this help screen.");
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            if (line.hasOption('h')) {
                printHelp(options);
                return;
            }
            if (line.hasOption('e')) {
                String file = line.getOptionValue("e");
                printArchiveContent(file);
                return;
            }
            if (line.hasOption('p')) {
                // initialise the member variable
                String portString = line.getOptionValue("p");
                try {
                    int port = Integer.parseInt(portString);
                    if (port < 1 || port > 65535) {
                        System.err.println("Port outside range.");
                        return;
                    }
                    ServerConfiguration.get().setPort(port);
                } catch (NumberFormatException ex) {
                    printHelp(options);
                    return;
                }
            }
            RunningMode.setServerType(RunningMode.ServerType.SERVER);
            RunningMode.logMode();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    main._shutdown();
                }
            });

            JettyServer server = startUp();
            BorgInstallation.getInstance().initialize();
            if (!line.hasOption('q')) {

                try {
                    java.awt.Desktop.getDesktop().browse(java.net.URI.create(server.getUrl()));
                } catch (Exception ex) {
                    log.info("Can't open web browser: " + ex.getMessage());
                }
            }
        } catch (ParseException ex) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + ex.getMessage());
            printHelp(options);
        }
    }

    private JettyServer _startUp(String... restPackageNames) {
        server = new JettyServer();
        server.start(restPackageNames);

        UserManager.setUserManager(new SingleUserManager());

        return server;
    }

    private void _shutdown() {
        if (server == null) {
            // Do nothing (server wasn't started).
            return;
        }
        synchronized (this) {
            if (shutdownInProgress == true) {
                // Another thread already called this method. There is nothing further to do.
                return;
            }
            shutdownInProgress = true;
        }
        log.info("Shutting down BorgButler web server...");
        server.stop();
        ButlerCache.getInstance().shutdown();
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("borgbutler-server", options);
    }

    private static void printArchiveContent(String fileName) {
        File file = new File(fileName);
        List<BorgFilesystemItem> fileList = ButlerCache.getInstance().getArchiveContent(file);
        boolean parseFormatExceptionPrinted = false;
        if (fileList != null && fileList.size() > 0) {
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat iso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
            iso.setTimeZone(tz);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
            File out = new File(FilenameUtils.getBaseName(fileName) + ".txt.gz");
            log.info("Writing file list to: " + out.getAbsolutePath());
            try (PrintWriter writer = new PrintWriter(new BufferedOutputStream(new GzipCompressorOutputStream(new FileOutputStream(out))))) {
                for (BorgFilesystemItem item : fileList) {
                    String time = item.getMtime();
                    if (time.indexOf('T') > 0) {
                        try {
                            Date date = df.parse(item.getMtime());
                            time = iso.format(date);
                        } catch (java.text.ParseException ex) {
                            if (!parseFormatExceptionPrinted) {
                                parseFormatExceptionPrinted = true;
                                log.error("Can't parse date: " + item.getMtime());
                            }
                        }
                    }
                    writer.write(item.getMode() + " " + item.getUser() + " "
                            + StringUtils.rightPad(FileUtils.byteCountToDisplaySize(item.getSize()), 10)
                            + " " + time + " " + item.getPath());
                    writer.write("\n");
                }
            } catch (IOException ex) {
                log.error("Can't write file '" + out.getAbsolutePath() + "': " + ex.getMessage());
            }
        }
        // 2018-12-04T22:44:58.924642
    }
}
