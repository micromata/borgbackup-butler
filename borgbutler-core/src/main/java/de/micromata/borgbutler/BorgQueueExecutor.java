package de.micromata.borgbutler;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.jobs.JobQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
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

    private JobQueue<String> getQueue(BorgRepoConfig config) {
        synchronized (queueMap) {
            String queueName = config != null ? config.getRepo() : "--NO_REPO--";
            JobQueue<String> queue = queueMap.get(queueName);
            if (queue == null) {
                queue = new JobQueue<>();
                queueMap.put(queueName, queue);
            }
            return queue;
        }
    }

    public BorgJob execute(BorgCommand command) {
        BorgJob job = new BorgJob(command);
        return execute(job);
    }

    public BorgJob execute(BorgJob job) {
        return (BorgJob)getQueue(job.getCommand().getRepoConfig()).append(job);
    }

    private BorgQueueExecutor() {
    }
}
