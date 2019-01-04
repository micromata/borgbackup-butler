package de.micromata.borgbutler.server.rest.queue;

import de.micromata.borgbutler.jobs.AbstractJob;
import lombok.Getter;
import lombok.Setter;

public class JsonJob {
    @Getter
    @Setter
    private boolean cancelledRequested;
    @Getter
    @Setter
    private AbstractJob.Status status;
    @Getter
    @Setter
    private String title;
    @Getter
    @Setter
    private String progressText;
}
