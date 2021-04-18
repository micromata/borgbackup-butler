package de.micromata.borgbutler.server

import de.micromata.borgbutler.cache.ButlerCache
import de.micromata.borgbutler.config.ConfigurationHandler
import de.micromata.borgbutler.config.ConfigurationHandler.Companion.init
import de.micromata.borgbutler.config.ConfigurationHandler.Companion.setConfigClazz
import de.micromata.borgbutler.server.user.SingleUserManager
import de.micromata.borgbutler.server.user.UserManager
import mu.KotlinLogging
import org.apache.commons.cli.*
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import java.awt.Desktop
import java.io.*
import java.net.URI
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.annotation.PreDestroy

private val log = KotlinLogging.logger {}

@SpringBootApplication
open class BorgButlerApplication {

    @Value("\${server.address}")
    private var serverAddress: String = "127.0.0.1"

    @Value("\${server.port}")
    private var serverPort = 9042

    private fun _start(args: Array<out String>) {
        setConfigClazz(ServerConfiguration::class.java)
        var borgButlerHome = System.getProperty("BorgButlerHome")
        if (borgButlerHome.isNullOrBlank()) {
            borgButlerHome = File(System.getProperty("user.home"), ".borgbutler").absolutePath
            System.setProperty("BorgButlerHome", borgButlerHome)
        }
        log.info { "Using BorgButlerHome: $borgButlerHome" }
        // Preread configuration to get setting development mode for WebConfig (logging not yet initialized, so
        // reread configuration later after logging is available for getting log information on errors etc.)
        val configuration = ConfigurationHandler.readJsonConfigfile(File(borgButlerHome))
        RunningMode.webDevelopment =
            RunningMode.runningInIDE || (configuration as? ServerConfiguration)?.webDevelopmentMode == true
        if (System.getProperty("LOG_PATH").isNullOrBlank()) {
            // Needed by logback-spring.xml
            System.setProperty("LOG_PATH", borgButlerHome)
        }
        SpringApplication.run(BorgButlerApplication::class.java, *args)

        if (borgButlerHome != null) {
            init(borgButlerHome)
        }
        // create Options object
        val options = Options()
        options.addOption(
            "e",
            "extract-archive-content",
            true,
            "Extracts the content of an archive cache file only (doesn't start the server). A complete file list of the archive will be extracted to stdout."
        )
        options.addOption("p", "port", true, "The default port for the web server.")
        options.addOption("q", "quiet", false, "Don't open browser automatically.")
        options.addOption("h", "help", false, "Print this help screen.")
        //options.addOption("homeDir", true, "Specify own home directory of butler. Default is $HOME/.borgbutler");
        val parser: CommandLineParser = DefaultParser()
        try {
            // parse the command line arguments
            val line = parser.parse(options, args)
            if (line.hasOption('h')) {
                printHelp(options)
                return
            }
            if (line.hasOption('e')) {
                val file = line.getOptionValue("e")
                printArchiveContent(file)
                return
            }
            if (line.hasOption('p')) {
                // initialise the member variable
                val portString = line.getOptionValue("p")
                try {
                    val port = portString.toInt()
                    if (port < 1 || port > 65535) {
                        System.err.println("Port outside range.")
                        return
                    }
                    ServerConfiguration.get().port = port
                } catch (ex: NumberFormatException) {
                    printHelp(options)
                    return
                }
            }
            RunningMode.logMode()

            UserManager.setUserManager(SingleUserManager())

            BorgInstallation.getInstance().initialize()

            // 0.0.0.0 for Docker installations.
            var url = "http://$serverAddress:$serverPort/"
            if (serverAddress.contains("0.0.0.0")) {
                log.info { "Server bind address: $url" }
                url = url.replace("0.0.0.0", "127.0.0.1")
            }
            val uri = URI.create(url)
            val quietMode = line.hasOption('q')
            if (!quietMode && RunningMode.desktopSupportsBrowse && RunningMode.headlessMode) {
                try {
                    log.info { "Trying to open your local web browser: $uri" }
                    Desktop.getDesktop().browse(uri)
                } catch (ex: Exception) {
                    log.info("Can't open web browser: " + ex.message, ex)
                    log.info("Desktop not available. Please open your browser manually: $uri")
                }
            } else {
                if (quietMode) {
                    log.info("Server started in quiet mode (option -q). Please open your browser manually: $uri")
                } else if (!RunningMode.desktopSupportsBrowse) {
                    log.info("Desktop not available. Please open your browser manually: $uri")
                } else if (RunningMode.headlessMode) {
                    log.info("Desktop not available in headless mode. Please open your browser manually: $uri")
                }
            }
        } catch (ex: ParseException) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + ex.message)
            printHelp(options)
        }
    }

    @EventListener(ApplicationReadyEvent::class)
    open fun startApp() {
        log.info { "Start-up of BorgButler finished. Have fun!" }
    }

    @PreDestroy
    open fun shutdownApp() {
        log.info("Shutting down BorgButler web server...")
        ButlerCache.getInstance().shutdown()
    }

    companion object {
        private val main = BorgButlerApplication()

        @JvmStatic
        fun main(vararg args: String) {
            main._start(args)
        }

        private fun printHelp(options: Options) {
            val formatter = HelpFormatter()
            formatter.printHelp("borgbutler-server", options)
        }

        private fun printArchiveContent(fileName: String) {
            val file = File(fileName)
            val fileList = ButlerCache.getInstance().getArchiveContent(file)
            var parseFormatExceptionPrinted = false
            if (fileList != null && fileList.size > 0) {
                val tz = TimeZone.getTimeZone("UTC")
                val iso: DateFormat =
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'") // Quoted "Z" to indicate UTC, no timezone offset
                iso.timeZone = tz
                val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S")
                val out = File(FilenameUtils.getBaseName(fileName) + ".txt.gz")
                log.info("Writing file list to: " + out.absolutePath)
                try {
                    PrintWriter(BufferedOutputStream(GzipCompressorOutputStream(FileOutputStream(out)))).use { writer ->
                        for (item in fileList) {
                            var time = item.mtime
                            if (time.indexOf('T') > 0) {
                                try {
                                    val date = df.parse(item.mtime)
                                    time = iso.format(date)
                                } catch (ex: java.text.ParseException) {
                                    if (!parseFormatExceptionPrinted) {
                                        parseFormatExceptionPrinted = true
                                        log.error("Can't parse date: " + item.mtime)
                                    }
                                }
                            }
                            writer.write(
                                item.mode + " " + item.user + " "
                                        + StringUtils.rightPad(FileUtils.byteCountToDisplaySize(item.size), 10)
                                        + " " + time + " " + item.path
                            )
                            writer.write("\n")
                        }
                    }
                } catch (ex: IOException) {
                    log.error("Can't write file '" + out.absolutePath + "': " + ex.message)
                }
            }
            // 2018-12-04T22:44:58.924642
        }
    }
}
