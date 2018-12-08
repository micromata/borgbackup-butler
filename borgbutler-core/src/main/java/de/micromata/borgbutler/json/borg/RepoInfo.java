package de.micromata.borgbutler.json.borg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.micromata.borgbutler.json.JsonUtils;
import lombok.Getter;
import lombok.Setter;

public class RepoInfo {
    @Getter
    @JsonProperty("security_dir")
    private String securityDir;
    @Getter
    private Cache cache;
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
}
