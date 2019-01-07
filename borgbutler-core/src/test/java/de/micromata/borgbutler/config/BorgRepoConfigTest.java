package de.micromata.borgbutler.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BorgRepoConfigTest {
    @Test
    void protectPassphraseTest()  {
        BorgRepoConfig config = new BorgRepoConfig();
        config.setRepo("Repo");
        config.setPassphrase("secret");
        config.setRsh("RSH");
        String[] variables = config.getEnvironmentVariables();
        test(variables,"BORG_REPO", "Repo");
        test(variables,"BORG_RSH", "RSH");
        test(variables,"BORG_PASSPHRASE", "******");
         variables = config.getEnvironmentVariables(true);
        test(variables,"BORG_REPO", "Repo");
        test(variables,"BORG_RSH", "RSH");
        test(variables,"BORG_PASSPHRASE", "secret");
    }

    private void test(String[] variables, String name, String value) {
        String val = null;
        for (String variable : variables) {
            if (!variable.startsWith(name))
                continue;
             val = variable;
        }
        assertEquals(name + "=" + value, val);
    }
}
