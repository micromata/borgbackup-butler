package de.micromata.borgbutler.jobs;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class JobQueue {
    private ConcurrentLinkedQueue<AbstractJob> queue = new ConcurrentLinkedQueue<>();
    private List<AbstractJob> done = new LinkedList<>();
    Executor executor = Executors.newSingleThreadExecutor();

    public AbstractJob appendOrJoin(AbstractJob job) {
        synchronized (queue) {
            if (queue.contains(job)) {
                for (AbstractJob queuedJob : queue) {
                    if (queuedJob.equals(job)) {
                        return queuedJob;
                    }
                }
            }
            queue.add(job);
            return job;
        }
    }
}
