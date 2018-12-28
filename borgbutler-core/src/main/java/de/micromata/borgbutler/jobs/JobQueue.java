package de.micromata.borgbutler.jobs;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobQueue<T> {
    private static final int MAX_DONE_JOBS_SIZE = 50;
    private Logger log = LoggerFactory.getLogger(JobQueue.class);
    private List<AbstractJob> queue = new ArrayList<>();
    private List<AbstractJob> doneJobs = new LinkedList<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public int getQueueSize() {
        return queue.size();
    }

    public List<AbstractJob> getDoneJobs() {
        return Collections.unmodifiableList(doneJobs);
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
            job.setFuture(executorService.submit(new CallableTask(job)));
        }
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
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                log.error(ex.getMessage(), ex);
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
                while (doneJobs.size() > MAX_DONE_JOBS_SIZE) {
                    doneJobs.remove(doneJobs.size() - 1);
                }
            }
        }
    }

    private class CallableTask implements Callable<T> {
        private AbstractJob<T> job;

        private CallableTask(AbstractJob<T> job) {
            this.job = job;
        }

        @Override
        public T call() throws Exception {
            if (job.isCancelledRequested()) {
                job.setStatus(AbstractJob.Status.CANCELLED);
                return null;
            }
            try {
                log.info("Starting job: " + job.getId());
                job.setStatus(AbstractJob.Status.RUNNING);
                T result = job.execute();
                if (!job.isFinished()) {
                    // Don't overwrite status failed set by job.
                    job.setStatus(AbstractJob.Status.DONE);
                }
                organizeQueue();
                return result;
            } catch (Exception ex) {
                log.error("Error while executing job '" + job.getId() + "': " + ex.getMessage(), ex);
                job.setStatus(AbstractJob.Status.FAILED);
                return null;
            }
        }
    }
}
