package de.micromata.borgbutler.jobs;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JobQueueTest {
    private Logger log = LoggerFactory.getLogger(JobQueueTest.class);
    // Bash script with simple counter and forced error if second argument is a valid counter.
    private static String bashScript = "#!/bin/bash\n" +
            "COUNTER=0\n" +
            "while [ $COUNTER -lt $1 ]; do\n" +
            "  if [ $COUNTER -eq $2 ]; then\n" +
            "    echo Error on counter $COUNTER >&2\n" +
            "    exit 2\n" +
            "  fi\n" +
            "  sleep 0.1\n" +
            "  echo The counter is $COUNTER >&2\n" +
            "  let COUNTER=COUNTER+1 \n" +
            "done\n" +
            "echo $COUNTER\n";

    private static File file;

    @BeforeAll
    static void createScript() throws IOException {
        file = File.createTempFile("counter", ".sh");
        file.deleteOnExit();
        FileUtils.write(file, bashScript, Charset.forName("UTF-8"));
        Files.setPosixFilePermissions(file.toPath(), PosixFilePermissions.fromString("rwxr-xr-x"));
    }

    @Test
    void queueTest() {
        JobQueue<String> queue = new JobQueue<>();
        assertEquals(0, queue.getQueueSize());
        queue.append(new TestJob(10, file));
        assertEquals(1, queue.getQueueSize());
        queue.append(new TestJob(5, 2, file));
        assertEquals(2, queue.getQueueSize());
        queue.append(new TestJob(10, file));
        assertEquals(2, queue.getQueueSize());
        TestJob job1 = (TestJob) queue.getQueuedJob(10);
        int counter = 100;
        while (job1.getStatus() != AbstractJob.Status.RUNNING && counter-- > 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        assertEquals(AbstractJob.Status.RUNNING, job1.getStatus());

        TestJob job = (TestJob) queue.getQueuedJob(5);
        assertEquals(AbstractJob.Status.QUEUED, job.getStatus());
        String result = job1.getResultObject();
        assertEquals("10\n", result);
        assertEquals(AbstractJob.Status.DONE, job1.getStatus());

        queue.append(new TestJob(10, file));
        queue.append(new TestJob(100, file));
        job1 = (TestJob) queue.getQueuedJob(10);
        assertEquals(AbstractJob.Status.QUEUED, job1.getStatus());

        job = (TestJob) queue.getQueuedJob(100);
        job.cancel();
        assertEquals(AbstractJob.Status.CANCELLED, job.getStatus());

        result = job1.getResultObject();
        assertEquals("10\n", result);

        assertEquals(0, queue.getQueueSize());
        List<AbstractJob<String>> doneJobs = queue.getDoneJobs();
        assertEquals(4, doneJobs.size());
        check(((TestJob) doneJobs.get(0)), AbstractJob.Status.DONE, "10");
        check(((TestJob) doneJobs.get(1)), AbstractJob.Status.CANCELLED, null);
        check(((TestJob) doneJobs.get(2)), AbstractJob.Status.FAILED, null);
        check(((TestJob) doneJobs.get(3)), AbstractJob.Status.DONE, "10");
    }

    @Test
    void queueStopRunningProcessTest() {
        JobQueue<String> queue = new JobQueue<>();
        assertEquals(0, queue.getQueueSize());
        queue.append(new TestJob(1000, file));
        queue.append(new TestJob(10, file));
        TestJob job = (TestJob) queue.getQueuedJob(1000);
        int counter = 100;
        while (!job.isExecuteStarted() && counter-- > 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        assertTrue(counter > 0);
        assertEquals(AbstractJob.Status.RUNNING, job.getStatus());
        job.cancel();
        counter = 100;
        while (job.getStatus() == AbstractJob.Status.RUNNING && counter-- > 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        assertTrue(counter > 0);
        assertEquals(AbstractJob.Status.CANCELLED, job.getStatus());
        job = (TestJob)queue.getQueuedJob(10);
        assertEquals("10\n", job.getResultObject());
        List<AbstractJob<String>> doneJobs = queue.getDoneJobs();
        assertEquals(2, doneJobs.size());
        check(((TestJob) doneJobs.get(0)), AbstractJob.Status.DONE, null);
        check(((TestJob) doneJobs.get(1)), AbstractJob.Status.CANCELLED, null);
    }

    private void check(TestJob job, AbstractJob.Status status, String result) {
        assertEquals(status, job.getStatus());
        if (result != null) {
            assertEquals(result + "\n", job.getResultObject());
        }
    }
}
