package de.micromata.borgbutler.jobs;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobQueue {
    private Logger log = LoggerFactory.getLogger(JobQueue.class);
    private List<AbstractJob> queue = new ArrayList<>();
    private List<AbstractJob> doneJobs = new LinkedList<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Runner runner = new Runner();

    public int getQueueSize() {
        return queue.size();
    }

    /**
     * Appends the job if not alread in the queue. Starts the execution if no execution thread is already running.
     *
     * @param job
     */
    public void append(AbstractJob job) {
        synchronized (queue) {
            for (AbstractJob queuedJob : queue) {
                if (Objects.equals(queuedJob.getId(), job.getId())) {
                    log.info("Job is already in the queue, don't run twice (OK): " + job.getId());
                    return;
                }
            }
            queue.add(job.setStatus(AbstractJob.Status.QUEUED));
        }
        run();
    }

    public AbstractJob getQueuedJob(Object id) {
        for (AbstractJob job : queue) {
            if (Objects.equals(job.getId(), id)) {
                return job;
            }
        }
        return null;
    }

    void waitForQueue(int seconds) {
        int counter = seconds / 10;
        while (CollectionUtils.isNotEmpty(queue) && counter > 0) {
            try {
                run(); // If not running!
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

    private void run() {
        synchronized (executorService) {
            if (!runner.running) {
                log.info("Starting job executor...");
                executorService.submit(runner);
            }
        }
    }

    private void organizeQueue() {
        synchronized (queue) {
            if (queue.isEmpty()) {
                return;
            }
            Iterator<AbstractJob> it = queue.iterator();
            while (it.hasNext()) {
                AbstractJob job = it.next();
                if (job.isFinished()) {
                    it.remove();
                    doneJobs.add(0, job);
                }
            }
        }
    }

    private class Runner implements Runnable {
        private boolean running;

        @Override
        public void run() {
            running = true;
            while (true) {
                AbstractJob job = null;
                synchronized (queue) {
                    organizeQueue();
                    if (queue.isEmpty()) {
                        running = false;
                        return;
                    }
                    for (AbstractJob queuedJob : queue) {
                        if (queuedJob.getStatus() == AbstractJob.Status.QUEUED) {
                            job = queuedJob;
                            break;
                        }
                    }
                }
                if (job == null) {
                    running = false;
                    return;
                }
                try {
                    log.info("Starting job: " + job.getId());
                    job.setStatus(AbstractJob.Status.RUNNING);
                    job.execute();
                    job.setStatus(AbstractJob.Status.DONE);
                } catch (Exception ex) {
                    log.error("Error while executing job '" + job.getId() + "': " + ex.getMessage(), ex);
                    job.setStatus(AbstractJob.Status.FAILED);
                }
            }
        }
    }
}
