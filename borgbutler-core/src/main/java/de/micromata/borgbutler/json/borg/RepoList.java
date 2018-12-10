package de.micromata.borgbutler.json.borg;

import lombok.Getter;

import java.io.Serializable;
import java.util.List;

/**
 * Result of borg list repo
 */
public class RepoList extends RepositoryMatcher implements Serializable {
    private static final long serialVersionUID = 1006757749929526034L;
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
