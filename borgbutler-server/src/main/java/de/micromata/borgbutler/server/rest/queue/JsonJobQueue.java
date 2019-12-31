package de.micromata.borgbutler.server.rest.queue;

import java.util.List;

public class JsonJobQueue {
    private String repo;
    private List<JsonJob> jobs;

    public String getRepo() {
        return this.repo;
    }

    public List<JsonJob> getJobs() {
        return this.jobs;
    }

    public JsonJobQueue setRepo(String repo) {
        this.repo = repo;
        return this;
    }

    public JsonJobQueue setJobs(List<JsonJob> jobs) {
        this.jobs = jobs;
        return this;
    }
}
