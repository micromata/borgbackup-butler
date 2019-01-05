package de.micromata.borgbutler.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobQueue<T> {
    private static final int MAX_DONE_JOBS_SIZE = 50;
    private static long jobSequence = 0;
    private Logger log = LoggerFactory.getLogger(JobQueue.class);
    private List<AbstractJob<T>> queue = new ArrayList<>();
    private List<AbstractJob<T>> doneJobs = new LinkedList<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private  static synchronized void setNextJobId(AbstractJob<?> job) {
        job.setUniqueJobNumber(jobSequence++);
    }

    public int getQueueSize() {
        return queue.size();
    }

    public Iterator<AbstractJob<T>> getQueueIterator() {
        return Collections.unmodifiableList(queue).iterator();
    }

    /**
     * Searches only for queued jobs (not done jobs).
     * @param uniqueJobNumber
     * @return The job if any job with the given unique job number is queued, otherwise null.
     */
    public AbstractJob<T> getQueuedJobByUniqueJobNumber(long uniqueJobNumber) {
        Iterator<AbstractJob<T>> it = queue.iterator();
        while (it.hasNext()) {
            AbstractJob<T> job = it.next();
            if (job.getUniqueJobNumber() == uniqueJobNumber) {
                return job;
            }
        }
        return null;
    }

    /**
     * Appends the job if not alread in the queue. Starts the execution if no execution thread is already running.
     *
     * @param job
     * @return The given job (if it's not already running or queued), otherwise the already running or queued job.
     */
    public AbstractJob<T> append(AbstractJob<T> job) {
        synchronized (queue) {
            for (AbstractJob<T> queuedJob : queue) {
                if (Objects.equals(queuedJob.getId(), job.getId())) {
                    log.info("Job is already in the queue, don't run twice (OK): " + job.getId());
                    return queuedJob;
                }
            }
            setNextJobId(job);
            queue.add(job.setStatus(AbstractJob.Status.QUEUED));
        }
        job.setFuture(executorService.submit(new CallableTask(job)));
        return job;
    }

    public AbstractJob getQueuedJob(Object id) {
        synchronized (queue) {
            for (AbstractJob job : queue) {
                if (Objects.equals(job.getId(), id)) {
                    return job;
                }
            }
        }
        return null;
    }

    private void organizeQueue() {
        synchronized (queue) {
            if (queue.isEmpty()) {
                return;
            }
            Iterator<AbstractJob<T>> it = queue.iterator();
            while (it.hasNext()) {
                AbstractJob<T> job = it.next();
                if (job.isFinished()) {
                    it.remove();
                    synchronized (doneJobs) {
                        doneJobs.add(0, job);
                    }
                }
                synchronized (doneJobs) {
                    while (doneJobs.size() > MAX_DONE_JOBS_SIZE) {
                        doneJobs.remove(doneJobs.size() - 1);
                    }
                }
            }
        }
    }

    private class CallableTask implements Callable<JobResult<T>> {
        private AbstractJob<T> job;

        private CallableTask(AbstractJob<T> job) {
            this.job = job;
        }

        @Override
        public JobResult<T> call() throws Exception {
            if (job.isCancellationRequested()) {
                job.setStatus(AbstractJob.Status.CANCELLED);
                return null;
            }
            try {
                log.info("Starting job: " + job.getId());
                job.setStatus(AbstractJob.Status.RUNNING);
                JobResult<T> result = job.execute();
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
