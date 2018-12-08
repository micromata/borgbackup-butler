package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.micromata.borgbutler.json.JsonUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class RepoList {
    @Getter
    private List<Archive> archives;
    @Getter
    private Encryption encryption;
    @Getter
    private Repository repository;
    @Getter
    @Setter
    @JsonIgnore
    private String originalJson;

    public String toString() {
        return JsonUtils.toJson(this, true);
    }

    public void updateFrom(RepoList repoList) {
        this.archives = repoList.archives;
        this.encryption = repoList.encryption;
        this.repository = repoList.getRepository();
        this.originalJson = repoList.originalJson;
    }
}
