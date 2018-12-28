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

public class JobQueueTest {
    private Logger log = LoggerFactory.getLogger(JobQueueTest.class);
    private static String bashScript = "#!/bin/bash\n" +
            "COUNTER=0\n" +
            "while [  $COUNTER -lt $1 ]; do\n" +
            "  sleep 0.1\n" +
            "  echo The counter is $COUNTER\n" +
            "  let COUNTER=COUNTER+1 \n" +
            "done";

    private static File file;

    @BeforeAll
    static void createScript() throws IOException {
        file = File.createTempFile("counter", ".sh");
        file.deleteOnExit();
        FileUtils.write(file, bashScript, Charset.forName("UTF-8"));
        Files.setPosixFilePermissions(file.toPath(), PosixFilePermissions.fromString("rwxr-xr-x"));
    }

    @Test
    void test() {
        TestJob job = new TestJob(10, file);
        job.execute();
    }
}
