package de.micromata.borgbutler;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.jobs.AbstractJob;
import de.micromata.borgbutler.jobs.JobQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A queue is important because Borg doesn't support parallel calls for one repository.
 * For each repository one single queue is allocated.
 */
public class BorgQueueExecutor {
    private Logger log = LoggerFactory.getLogger(BorgQueueExecutor.class);
    private static final BorgQueueExecutor instance = new BorgQueueExecutor();
    private static final String NONE_REPO_QUEUE = "--NO_REPO--";

    public static BorgQueueExecutor getInstance() {
        return instance;
    }

    // key is the repo name.
    private Map<String, JobQueue<String>> queueMap = new HashMap<>();

    public BorgQueueStatistics getStatistics() {
        BorgQueueStatistics statistics = new BorgQueueStatistics();
        Iterator<JobQueue<String>> it = queueMap.values().iterator();
        while (it.hasNext()) {
            JobQueue<?> queue = it.next();
            statistics.totalNumberOfQueues++;
            int queueSize = queue.getQueueSize();
            if (queueSize > 0) {
                statistics.numberOfActiveQueues++;
                statistics.numberOfRunningAndQueuedJobs += queueSize;
            }
            int oldJobsSize = queue.getOldJobsSize();
            if (oldJobsSize > 0) {
                statistics.numberOfOldJobs += oldJobsSize;
            }
        }
        return statistics;
    }

    /**
     * @return A list of all repos with queues.
     */
    public List<String> getRepos() {
        List<String> list = new ArrayList<>();
        synchronized (queueMap) {
            list.addAll(queueMap.keySet());
        }
        Collections.sort(list);
        return list;
    }

    public void cancelJob(long uniqueJobNumber) {
        AbstractJob<?> job = null;
        JobQueue<?> queue = null;
        Iterator<JobQueue<String>> it = queueMap.values().iterator();
        while (it.hasNext()) {
            queue = it.next();
            job = queue.getQueuedJobByUniqueJobNumber(uniqueJobNumber);
            if (job != null) {
                break;
            }
        }
        if (job == null) {
            log.info("Can't cancel job #" + uniqueJobNumber + ". Not found as queued job (may-be already cancelled or finished). Nothing to do.");
            return;
        }
        job.cancel();
        queue.refreshQueue();
    }

    /**
     * For displaying purposes.
     *
     * @param repoConfig
     * @param oldJobs If false, the running and queued jobs are returned, otherwise the done ones.
     * @return A list of all jobs of the queue (as copies).
     */
    public List<BorgJob<?>> getJobListCopy(BorgRepoConfig repoConfig, boolean oldJobs) {
        JobQueue<String> origQueue = getQueue(repoConfig);
        List<BorgJob<?>> jobList = new ArrayList<>();
        if (origQueue == null) {
            return jobList;
        }
        synchronized (origQueue) {
            Iterator<AbstractJob<String>> it = oldJobs ? origQueue.getOldJobsIterator() : origQueue.getQueueIterator();
            while (it.hasNext()) {
                AbstractJob<String> origJob = it.next();
                if (!(origJob instanceof BorgJob)) {
                    log.error("Oups, only BorgJobs are supported. Ignoring unexpected job: " + origJob.getClass());
                    continue;
                }
                BorgJob<?> borgJob = ((BorgJob<?>) origJob).clone();
                jobList.add(borgJob);
            }
        }
        return jobList;
    }

    private JobQueue<String> getQueue(BorgRepoConfig repoConfig) {
        synchronized (queueMap) {
            return queueMap.get(getQueueName(repoConfig));
        }
    }

    private JobQueue<String> ensureAndGetQueue(BorgRepoConfig repoConfig) {
        synchronized (queueMap) {
            String queueName = getQueueName(repoConfig);
            JobQueue<String> queue = getQueue(repoConfig);
            if (queue == null) {
                queue = new JobQueue<>();
                queueMap.put(queueName, queue);
            }
            return queue;
        }
    }

    private String getQueueName(BorgRepoConfig repoConfig) {
        return repoConfig != null ? repoConfig.getId() : NONE_REPO_QUEUE;
    }

    public BorgJob<Void> execute(BorgCommand command) {
        BorgJob<Void> job = new BorgJob<Void>(command);
        return execute(job);
    }

    @SuppressWarnings("unchecked")
    public <T> BorgJob<T> execute(BorgJob<T> job) {
        return (BorgJob<T>) ensureAndGetQueue(job.getCommand().getRepoConfig()).append(job);
    }

    private BorgQueueExecutor() {
    }
}
