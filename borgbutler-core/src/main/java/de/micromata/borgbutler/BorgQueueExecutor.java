package de.micromata.borgbutler;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.jobs.AbstractJob;
import de.micromata.borgbutler.jobs.JobQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A queue is important because Borg doesn't support parallel calls for one repository.
 * For each repository one single queue is allocated.
 */
public class BorgQueueExecutor {
    private Logger log = LoggerFactory.getLogger(BorgQueueExecutor.class);
    private static final BorgQueueExecutor instance = new BorgQueueExecutor();

    public static BorgQueueExecutor getInstance() {
        return instance;
    }

    // key is the repo name.
    private Map<String, JobQueue<String>> queueMap = new HashMap<>();

    /**
     * For displaying purposes.
     *
     * @param repo
     * @return A list of all jobs of the queue (as copies).
     */
    public List<BorgJob<?>> getJobListCopy(String repo) {
        JobQueue<String> origQueue = getQueue(repo);
        List<BorgJob<?>> jobList = new ArrayList<>();
        for (AbstractJob<String> origJob : origQueue.getQueue()) {
            if (!(origJob instanceof BorgJob)) {
                log.error("Oups, only BorgJobs are supported. Ignoring unexpected job: " + origJob.getClass());
                continue;
            }
            BorgJob<?> borgJob = ((BorgJob<?>) origJob).clone();
            jobList.add(borgJob);
        }
        return jobList;
    }

    private JobQueue<String> getQueue(String repo) {
        return queueMap.get(getQueueName(repo));
    }

    private JobQueue<String> ensureAndGetQueue(String repo) {
        synchronized (queueMap) {
            String queueName = getQueueName(repo);
            JobQueue<String> queue = getQueue(queueName);
            if (queue == null) {
                queue = new JobQueue<>();
                queueMap.put(queueName, queue);
            }
            return queue;
        }
    }

    private JobQueue<String> ensureAndGetQueue(BorgRepoConfig config) {
        return ensureAndGetQueue(config != null ? config.getRepo() : null);
    }

    private String getQueueName(String repo) {
        return repo != null ? repo : "--NO_REPO--";
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
