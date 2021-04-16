package de.micromata.borgbutler.server.rest

import de.micromata.borgbutler.BorgQueueExecutor
import de.micromata.borgbutler.config.ConfigurationHandler
import de.micromata.borgbutler.jobs.AbstractJob
import de.micromata.borgbutler.json.JsonUtils
import de.micromata.borgbutler.json.borg.ProgressInfo
import de.micromata.borgbutler.server.rest.queue.JsonJob
import de.micromata.borgbutler.server.rest.queue.JsonJobQueue
import mu.KotlinLogging
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/rest/jobs")
class JobsRest {
    /**
     * @param repo If given, only the job queue of the given repo will be returned.
     * @param testMode If true, then a test job list is created.
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @return Job queues as json string.
     * @see JsonUtils.toJson
     */
    @GetMapping
    fun getJobs(
        @RequestParam("repo", required = false) repo: String?,
        @RequestParam("testMode", required = false) testMode: Boolean?,
        @RequestParam("oldJobs", required = false) oldJobs: Boolean?,
        @RequestParam("prettyPrinter", required = false) prettyPrinter: Boolean?
    ): String {
        log.debug("getJobs repo=$repo, oldJobs=$oldJobs")
        if (testMode == true) {
            // Return dynamic test queue:
            return returnTestList(oldJobs, prettyPrinter)
        }
        var validRepo = false
        if (StringUtils.isNotBlank(repo) && "null" != repo && "undefined" != repo) {
            validRepo = true
        }
        val borgQueueExecutor = BorgQueueExecutor.getInstance()
        val queueList = mutableListOf<JsonJobQueue>()
        if (validRepo) { // Get only the queue of the given repo:
            val queue = getQueue(repo, oldJobs)
            if (queue != null) {
                queueList.add(queue)
            }
        } else { // Get all the queues (of all repos).
            for (rep in borgQueueExecutor.getRepos()) {
                val queue = getQueue(rep, oldJobs)
                if (queue != null) {
                    queueList.add(queue)
                }
            }
        }
        return JsonUtils.toJson(queueList, prettyPrinter)
    }

    private fun getQueue(repo: String?, oldJobs: Boolean?): JsonJobQueue? {
        val borgQueueExecutor: BorgQueueExecutor = BorgQueueExecutor.getInstance()
        val repoConfig = ConfigurationHandler.getConfiguration().getRepoConfig(repo) ?: return null
        val borgJobList = borgQueueExecutor.getJobListCopy(repoConfig, oldJobs == true)
        if (CollectionUtils.isEmpty(borgJobList)) return null
        val queue: JsonJobQueue = JsonJobQueue().setRepo(repoConfig.getDisplayName())
        queue.setJobs(mutableListOf())
        for (borgJob in borgJobList) {
            val job = JsonJob(borgJob)
            queue.getJobs().add(job)
        }
        return queue
    }

    /**
     * @param uniqueJobNumberString The id of the job to cancel.
     */
    @GetMapping("/cancel")
    fun cancelJob(@RequestParam("uniqueJobNumber") uniqueJobNumberString: String) {
        val uniqueJobNumber =
            try {
                uniqueJobNumberString.toLong()
            } catch (ex: NumberFormatException) {
                log.error("Can't cancel job, because unique job number couln't be parsed (long value expected): $uniqueJobNumberString")
                return
            }
        BorgQueueExecutor.getInstance().cancelJob(uniqueJobNumber)
    }

    /**
     * Only for test purposes and development.
     *
     * @param oldJobs
     * @param prettyPrinter
     * @return
     */
    private fun returnTestList(oldJobs: Boolean?, prettyPrinter: Boolean?): String {
        var list = if (oldJobs == true) oldJobsTestList else testList
        if (list == null) {
            list = mutableListOf()
            var uniqueJobNumber: Long = 100000
            var queue: JsonJobQueue = JsonJobQueue().setRepo("My Computer")
            addTestJob(queue, "info", "my-macbook", 0, 2342, uniqueJobNumber++, oldJobs == true)
            addTestJob(queue, "list", "my-macbook", -1, -1, uniqueJobNumber++, oldJobs == true)
            list.add(queue)
            queue = JsonJobQueue().setRepo("My Server")
            addTestJob(queue, "list", "my-server", 0, 1135821, uniqueJobNumber++, oldJobs == true)
            addTestJob(queue, "info", "my-server", -1, -1, uniqueJobNumber++, oldJobs == true)
            list.add(queue)
            if (oldJobs == true) {
                oldJobsTestList = list
            } else {
                testList = list
            }
        } else if (oldJobs != true) {
            for (jobQueue in list) {
                for (job in jobQueue.getJobs()) {
                    if (job.getStatus() != AbstractJob.Status.RUNNING) continue
                    var current: Long = job.getProgressInfo().getCurrent()
                    val total: Long = job.getProgressInfo().getTotal()
                    current += if (StringUtils.startsWith(job.getProgressInfo().getMessage(), "Calculating")) {
                        // Info is a faster operation:
                        (Math.random() * total / 5).toLong()
                    } else {
                        // than get the complete archive file list:
                        (Math.random() * total / 30).toLong()
                    }
                    if (current > total) {
                        current = 0 // Reset to beginning.
                    }
                    job.getProgressInfo().setCurrent(current)
                    if (job.getProgressText().startsWith("Calculating")) {
                        job.getProgressInfo()
                            .setMessage("Calculating statistics...  " + Math.round((100 * current / total).toFloat()) + "%")
                    }
                    job.buildProgressText()
                }
            }
        }
        return JsonUtils.toJson(list, prettyPrinter)
    }

    /**
     * Only for test purposes and development.
     *
     * @param queue
     * @param operation
     * @param host
     * @param current
     * @param total
     * @return
     */
    private fun addTestJob(
        queue: JsonJobQueue,
        operation: String,
        host: String,
        current: Long,
        total: Long,
        uniqueNumber: Long,
        oldJobs: Boolean
    ): JsonJob {
        val progressInfo = ProgressInfo()
            .setCurrent(current)
            .setTotal(total)
        val job: JsonJob = JsonJob()
            .setProgressInfo(progressInfo)
            .setStatus(AbstractJob.Status.QUEUED)
        if ("info" == operation) {
            progressInfo.setMessage("Calculating statistics... ")
            job.setDescription("Loading info of archive '" + host + "-2018-12-05T23:10:33' of repo '" + queue.getRepo() + "'.")
                .setCommandLineAsString("borg info --json --log-json --progress ssh://...:23/./backups/$host::$host-2018-12-05T23:10:33")
        } else {
            progressInfo.setMessage("Getting file list... ")
            job.setDescription("Loading list of files of archive '" + host + "-2018-12-05T17:30:38' of repo '" + queue.getRepo() + "'.")
                .setCommandLineAsString("borg list --json-lines ssh://...:23/./backups/$host::$host-2018-12-05T17:30:38")
        }
        job.buildProgressText()
        if (current >= 0) {
            job.setStatus(AbstractJob.Status.RUNNING)
        } else {
            job.setStatus(AbstractJob.Status.QUEUED)
        }
        if (queue.getJobs() == null) {
            queue.setJobs(ArrayList<JsonJob>())
        }
        job.setUniqueJobNumber(uniqueNumber)
        if (oldJobs) {
            job.setStatus(if (uniqueNumber % 2 == 0L) AbstractJob.Status.CANCELLED else AbstractJob.Status.DONE)
        }
        queue.getJobs().add(job)
        return job
    }

    companion object {
        private var testList: MutableList<JsonJobQueue>? = null
        private var oldJobsTestList: MutableList<JsonJobQueue>? = null
    }
}
