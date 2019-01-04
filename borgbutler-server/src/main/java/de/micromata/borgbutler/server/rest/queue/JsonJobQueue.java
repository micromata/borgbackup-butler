package de.micromata.borgbutler.server.rest.queue;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class JsonJobQueue {
    @Getter
    @Setter
    private String repo;
    @Getter
    @Setter
    private List<JsonJob> jobs;
}
