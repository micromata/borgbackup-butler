package de.micromata.borgbutler

import de.micromata.borgbutler.config.ConfigurationHandler.Companion.getConfiguration
import de.micromata.borgbutler.demo.DemoRepos
import de.micromata.borgbutler.jobs.AbstractCommandLineJob
import de.micromata.borgbutler.jobs.JobResult
import de.micromata.borgbutler.json.JsonUtils
import de.micromata.borgbutler.json.borg.ProgressInfo
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.environment.EnvironmentUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * A queue is important because Borg doesn't support parallel calls for one repository.
 * For each repository one single queue is allocated.
 */
open class BorgJob<T> : AbstractCommandLineJob, Cloneable {
    private val log = LoggerFactory.getLogger(BorgJob::class.java)
    var command: BorgCommand? = null
        private set

    /**
     * Some jobs may store here the result of the command (e. g. [BorgCommands.listArchiveContent]).
     */
    @JvmField
    var payload: T? = null
    var progressInfo: ProgressInfo? = null
        private set

    constructor(command: BorgCommand) {
        this.command = command
        setWorkingDirectory(command.workingDir)
        setDescription(command.description)
    }

    private constructor() {}

    override fun buildCommandLine(): CommandLine? {
        if (command == null) {
            return null
        }
        val borgCommand = getConfiguration()!!.borgCommand
        if (StringUtils.isBlank(borgCommand)) {
            log.error("Can't run empty borg command.")
            return null
        }
        val commandLine = CommandLine(borgCommand)
        commandLine.addArgument(command!!.command)
        if (command!!.params != null) {
            for (param in command!!.params) {
                if (param != null) commandLine.addArgument(param)
            }
        }
        if (command!!.repoArchive != null) {
            commandLine.addArgument(command!!.repoArchive)
        }
        if (command!!.args != null) {
            for (arg in command!!.args) {
                if (arg != null) commandLine.addArgument(arg)
            }
        }
        return commandLine
    }

    override fun processStdErrLine(line: String, level: Int) {
        if (StringUtils.startsWith(line, "{\"message")) {
            val message = JsonUtils.fromJson(ProgressInfo::class.java, line)
            if (message != null) {
                progressInfo = message
                return
            }
        }
        super.processStdErrLine(line, level)
    }

    // For MacOS BORG_PASSCOMMAND="security find-generic-password -a $USER -s borg-passphrase -w"
    @get:Throws(IOException::class)
    override val environment: Map<String?, String?>?
        protected get() {
            val repoConfig = command!!.repoConfig ?: return null
            val env = EnvironmentUtils.getProcEnvironment()
            val variables = repoConfig.getEnvironmentVariables(true)
            for (variable in variables) {
                // For MacOS BORG_PASSCOMMAND="security find-generic-password -a $USER -s borg-passphrase -w"
                val environmentVariable = variable.replace("\$USER", System.getProperty("user.name"))
                addEnvironmentVariable(env, environmentVariable)
            }
            return env
        }

    override fun execute(): JobResult<String>? {
        return if (command!!.repoConfig != null && DemoRepos.isDemo(command!!.repoConfig.repo)) {
            DemoRepos.execute(this)
        } else super.execute()
    }

    public override fun clone(): BorgJob<*> {
        val clone: BorgJob<*> = BorgJob<Any>()
        if (command != null) {
            // Needed for getting environment variables: JsonJob of borgbutler-server.
            clone.command = BorgCommand().setRepoConfig(command!!.repoConfig)
        }
        clone.uniqueJobNumber = uniqueJobNumber
        clone.title = title
        clone.setExecuteStarted(isExecuteStarted)
        clone.setCommandLineAsString(getCommandLineAsString())
        clone.isCancellationRequested = isCancellationRequested
        clone.status = status
        clone.setWorkingDirectory(workingDirectory)
        clone.setDescription(description)
        if (progressInfo != null) {
            clone.setProgressInfo(progressInfo!!.clone())
        }
        clone.createTime = createTime
        clone.startTime = startTime
        clone.stopTime = stopTime
        return clone
    }

    override fun cleanUp() {
        super.cleanUp()
        payload = null
    }

    fun getPayload(): T? {
        return payload
    }

    protected fun setProgressInfo(progressInfo: ProgressInfo?): BorgJob<T> {
        this.progressInfo = progressInfo
        return this
    }
}
