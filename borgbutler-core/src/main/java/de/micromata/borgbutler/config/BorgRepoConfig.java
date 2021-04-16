package de.micromata.borgbutler.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.micromata.borgbutler.json.JsonUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BorgRepoConfig {
    /**
     * A name describing this config. Only used for displaying purposes.
     */
    private String displayName;
    private String repo;
    private String rsh;
    private String passphrase;
    private String passwordCommand;
    private String id;

    @JsonIgnore
    public String[] getEnvironmentVariables() {
        return getEnvironmentVariables(false);
    }

    public String[] getEnvironmentVariables(boolean showPassphrase) {
        List<String> variables = new ArrayList<>();
        addVariable(variables, "BORG_REPO", repo);
        addVariable(variables, "BORG_RSH", rsh);
        if (StringUtils.isNotBlank(passphrase)) {
            addVariable(variables, "BORG_PASSPHRASE", showPassphrase ? passphrase : "******");
        }
        addVariable(variables, "BORG_PASSCOMMAND", passwordCommand);
        return variables.toArray(new String[variables.size()]);
    }

    private void addVariable(List<String> list, String variable, String value) {
        if (StringUtils.isBlank(value)) return;
        list.add(variable + "=" + value);
    }

    public void copyFrom(BorgRepoConfig other) {
        this.displayName = other.displayName;
        this.repo = other.repo;
        this.rsh = other.rsh;
        this.passphrase = other.passphrase;
        this.passwordCommand = other.passwordCommand;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getRepo() {
        return this.repo;
    }

    public String getRsh() {
        return this.rsh;
    }

    public String getPassphrase() {
        return this.passphrase;
    }

    public String getPasswordCommand() {
        return this.passwordCommand;
    }

    public String getId() {
        return this.id;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public void setRsh(String rsh) {
        this.rsh = rsh;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public void setPasswordCommand(String passwordCommand) {
        this.passwordCommand = passwordCommand;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }
}
