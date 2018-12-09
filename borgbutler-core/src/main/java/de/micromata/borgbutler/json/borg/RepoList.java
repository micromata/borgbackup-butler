package de.micromata.borgbutler.json.borg;

import lombok.Getter;

import java.util.List;

public class RepoList extends RepositoryMatcher {
    @Getter
    private List<Archive> archives;
    @Getter
    private Encryption encryption;

    public void updateFrom(RepoList repoList) {
        super.updateFrom(repoList);
        this.archives = repoList.archives;
        this.encryption = repoList.encryption;
    }
}
