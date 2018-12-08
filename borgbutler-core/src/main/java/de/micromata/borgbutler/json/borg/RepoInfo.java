package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public class RepoInfo extends RepositoryMatcher {
    @Getter
    @JsonProperty("security_dir")
    private String securityDir;
    @Getter
    private Cache cache;
    @Getter
    private Encryption encryption;

    public void updateFrom(RepoInfo repoInfo) {
        super.updateFrom(repoInfo);
        this.securityDir = repoInfo.securityDir;
        this.cache = repoInfo.cache;
        this.encryption = repoInfo.encryption;
    }
}
