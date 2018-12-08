package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.micromata.borgbutler.json.JsonUtils;
import lombok.Getter;
import lombok.Setter;

public abstract class RepositoryMatcher {
    @Getter
    protected Repository repository;
    @Getter
    @Setter
    @JsonIgnore
    protected String originalJson;

    public String toString() {
        return JsonUtils.toJson(this, true);
    }

    public void updateFrom(RepositoryMatcher from) {
        this.repository = from.repository;
        this.originalJson = from.originalJson;
    }

    public boolean matches(String identifier) {
        if (repository == null) {
            return false;
        }
        return identifier.equals(repository.getId()) || identifier.equals(repository.getName())
                || identifier.equals(repository.getLocation());
    }
}
