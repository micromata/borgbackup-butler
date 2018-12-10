package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.micromata.borgbutler.json.JsonUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public abstract class RepositoryMatcher implements Serializable {
    private static final long serialVersionUID = -3672403959096363628L;

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
        if (repository == null || identifier == null) {
            return false;
        }
        return identifier.equals(repository.getId()) || identifier.equals(repository.getName())
                || identifier.equals(repository.getLocation());
    }
}
