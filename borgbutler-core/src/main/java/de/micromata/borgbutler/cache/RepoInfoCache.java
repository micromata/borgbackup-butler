package de.micromata.borgbutler.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.micromata.borgbutler.json.JsonUtils;
import de.micromata.borgbutler.json.borg.RepoInfo;
import de.micromata.borgbutler.json.borg.Repository;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class RepoInfoCache {
    private static Logger log = LoggerFactory.getLogger(RepoInfoCache.class);
    public static final String CACHE_REPOS_FILENAME = "cache-repos.json";

    @JsonIgnore
    private File cacheReposFile;
    @JsonProperty
    private List<RepoInfo> repositories = new ArrayList<>();

    public RepoInfo getRepoInfo(String idOrName) {
        if (idOrName == null) {
            return null;
        }
        for (RepoInfo repoInfo : repositories) {
            Repository repository = repoInfo.getRepository();
            if (repository == null) {
                continue;
            }
            if (idOrName.equals(repository.getId()) || idOrName.equals(repository.getName()) || idOrName.equals(repository.getLocation())) {
                return repoInfo;
            }
        }
        return null;
    }

    public void upsert(RepoInfo repoInfo) {
        Repository repository = repoInfo.getRepository();
        if (repository == null) {
            log.error("Oups, no repository given in RepoInfo (ignoring it): " + repoInfo);
            return;
        }
        RepoInfo existingRepo = getRepoInfo(repository.getId());
        if (existingRepo == null) {
            repositories.add(repoInfo);
        } else {
            existingRepo.updateFrom(repoInfo);
        }
    }

    public void read() {
        try {
            String json = FileUtils.readFileToString(cacheReposFile, Charset.forName("UTF-8"));
            RepoInfoCache readCache = JsonUtils.fromJson(this.getClass(), json);
            if (readCache != null) {
                this.repositories = readCache.repositories;
            } else {
                log.error("Error while parsing repos cache: " + cacheReposFile.getAbsolutePath());
            }
        } catch (IOException ex) {
            log.error("Error while trying to read repos cache file '" + cacheReposFile.getAbsolutePath() + "': "
                    + ex.getMessage(), ex);
        }
    }

    public void save() {
        log.info("Saving repo infos to cache file: " + cacheReposFile);
        String json = JsonUtils.toJson(this);
        try {
            FileUtils.write(cacheReposFile, json, Charset.forName("UTF-8"));
        } catch (IOException ex) {
            log.error("Error while trying to write repos cache file '" + cacheReposFile.getAbsolutePath() + "': "
                    + ex.getMessage(), ex);
        }
    }

    RepoInfoCache(File cacheDir) {
        cacheReposFile = new File(cacheDir, CACHE_REPOS_FILENAME);
    }
}
