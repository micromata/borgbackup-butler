package de.micromata.borgbutler.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BorgConfigTest {

    @Test
    void versionCompareTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            BorgConfig.compareVersions(null, "");
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            BorgConfig.compareVersions("", "");
        });
        Assertions.assertEquals(-1, BorgConfig.compareVersions("1.1.8", "1.1.16"));
        Assertions.assertEquals(0, BorgConfig.compareVersions("1.1.8", "1.1.8"));
        Assertions.assertEquals(1, BorgConfig.compareVersions("1.1.16", "1.1.8"));
    }
}
