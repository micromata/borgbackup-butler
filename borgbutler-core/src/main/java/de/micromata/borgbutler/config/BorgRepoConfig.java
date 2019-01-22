package de.micromata.borgbutler.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BorgRepoConfig {
    /**
     * A name describing this config. Only used for displaying purposes.
     */
    @Getter
    @Setter
    private String displayName;
    @Getter
    @Setter
    private String repo;
    @Getter
    @Setter
    private String rsh;
    @Getter
    @Setter
    private String passphrase;
    @Getter
    @Setter
    private String passwordCommand;
    @Getter
    @Setter
    private String id;

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
        this.passphrase = other.passphrase;
        this.passwordCommand = other.passwordCommand;
   }
}
