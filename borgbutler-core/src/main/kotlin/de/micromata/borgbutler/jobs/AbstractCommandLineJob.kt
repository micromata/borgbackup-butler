package de.micromata.borgbutler.jobs

import de.micromata.borgbutler.config.Definitions
import mu.KotlinLogging
import org.apache.commons.exec.*
import org.apache.commons.exec.environment.EnvironmentUtils
import org.apache.commons.io.output.ByteArrayOutputStream
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.IOException

private val log = KotlinLogging.logger {}

/**
 * A queue is important because Borg doesn't support parallel calls for one repository.
 * For each repository one single queue is allocated.
 */
abstract class AbstractCommandLineJob : AbstractJob<String>() {
    private var watchdog: ExecuteWatchdog? = null
    var isExecuteStarted = false
        private set
    private var commandLine: CommandLine? = null

    /**
     * The command line as string. This property is also used as ID for detecting multiple borg calls.
     */
    private var commandLineAsString: String? = null
    var workingDirectory: File? = null
        private set
    var description: String? = null
        private set
    protected var outputStream = ByteArrayOutputStream()
    protected var errorOutputStream = ByteArrayOutputStream()

    @JvmField
    protected var logError = true
    protected abstract fun buildCommandLine(): CommandLine?
    override fun getId(): Any {
        return getCommandLineAsString()!!
    }

    fun getCommandLineAsString(): String? {
        if (commandLine == null) {
            commandLine = buildCommandLine()
        }
        if (commandLine == null) {
            return null
        }
        if (commandLineAsString == null) {
            commandLineAsString = commandLine!!.executable + " " + StringUtils.join(
                commandLine!!.arguments, " "
            )
        }
        return commandLineAsString
    }

    override fun execute(): JobResult<String>? {
        getCommandLineAsString()
        if (commandLine == null) {
            return null
        }
        val executor = DefaultExecutor()
        if (workingDirectory != null) {
            executor.workingDirectory = workingDirectory
        }
        //executor.setExitValue(2);
        watchdog = ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT)
        executor.watchdog = watchdog
        //  ExecuteResultHandler handler = new DefaultExecuteResultHandler();
        val streamHandler = PumpStreamHandler(object : LogOutputStream() {
            override fun processLine(line: String, level: Int) {
                processStdOutLine(line, level)
            }
        }, object : LogOutputStream() {
            override fun processLine(line: String, level: Int) {
                processStdErrLine(line, level)
            }
        })
        executor.streamHandler = streamHandler
        val msg =
            if (StringUtils.isNotBlank(description)) "$description ('$commandLineAsString')..." else "Executing '$commandLineAsString'..."
        log.info(msg)
        //log.info("Environment: ${environment?.entries?.joinToString { "${it.key}='${it.value}'" }}")
        isExecuteStarted = true
        val result = JobResult<String>()
        try {
            executor.execute(commandLine, environment)
            result.status = JobResult.Status.OK
            log.info("$msg Done.")
        } catch (ex: Exception) {
            result.status = JobResult.Status.ERROR
            if (logError && !isCancellationRequested && status != Status.CANCELLED) {
                log.error("Execution failed for job: '" + commandLineAsString + "': " + ex.message)
                log.error(
                    "Error output of job '" + commandLineAsString + "': "
                            + getErrorString(2000)
                )
            }
            failed()
        }
        result.resultObject = outputStream.toString(Definitions.STD_CHARSET)
        return result
    }

    /**
     * @param maxlength The result string will be abbreviated (in the middle).
     * @return
     * @see StringUtils.abbreviateMiddle
     */
    fun getErrorString(maxlength: Int): String {
        return StringUtils.abbreviateMiddle(
            errorOutputStream.toString(Definitions.STD_CHARSET),
            "\n    [... ***** error log abbreviated ***** ...]\n", maxlength
        )
    }

    open fun processStdOutLine(line: String, level: Int) {
        //log.info(line);
        try {
            outputStream.write(line.toByteArray())
            outputStream.write("\n".toByteArray())
        } catch (ex: IOException) {
            log.error(ex.message, ex)
        }
    }

    open fun processStdErrLine(line: String, level: Int) {
        //log.info(line);
        try {
            errorOutputStream.write(line.toByteArray())
            errorOutputStream.write("\n".toByteArray())
        } catch (ex: IOException) {
            log.error(ex.message, ex)
        }
    }

    override fun cancelRunningProcess() {
        if (watchdog != null) {
            log.info("Cancelling job #$uniqueJobNumber: $id")
            watchdog!!.destroyProcess()
            watchdog = null
        }
    }

    @get:Throws(IOException::class)
    protected open val environment: Map<String?, String?>?
        get() = null

    protected fun addEnvironmentVariable(env: Map<String?, String?>?, name: String, value: String) {
        if (StringUtils.isNotBlank(value)) {
            EnvironmentUtils.addVariableToEnvironment(env, "$name=$value")
        }
    }

    /**
     * @param env
     * @param variable Variable in format "variable=value".
     */
    protected fun addEnvironmentVariable(env: Map<String?, String?>?, variable: String?) {
        if (StringUtils.isNotBlank(variable)) {
            EnvironmentUtils.addVariableToEnvironment(env, variable)
        }
    }

    /**
     * Frees the output streams.
     * Should be called after a job was done, failed or cancelled while running.
     */
    open fun cleanUp() {
        log.debug("Freeing resources of job: $commandLineAsString")
        outputStream = ByteArrayOutputStream()
        errorOutputStream = ByteArrayOutputStream()
    }

    protected fun setExecuteStarted(executeStarted: Boolean): AbstractCommandLineJob {
        isExecuteStarted = executeStarted
        return this
    }

    protected fun setCommandLineAsString(commandLineAsString: String?): AbstractCommandLineJob {
        this.commandLineAsString = commandLineAsString
        return this
    }

    fun setWorkingDirectory(workingDirectory: File?): AbstractCommandLineJob {
        this.workingDirectory = workingDirectory
        return this
    }

    fun setDescription(description: String?): AbstractCommandLineJob {
        this.description = description
        return this
    }
}
