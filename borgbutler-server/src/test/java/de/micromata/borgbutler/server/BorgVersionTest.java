package de.micromata.borgbutler.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BorgVersionTest {

    @Test
    void versionCompareTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            BorgVersion.compareVersions(null, "");
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            BorgVersion.compareVersions("", "");
        });
        Assertions.assertEquals(-1, BorgVersion.compareVersions("1.1.8", "1.1.16"));
        Assertions.assertEquals(0, BorgVersion.compareVersions("1.1.8", "1.1.8"));
        Assertions.assertEquals(1, BorgVersion.compareVersions("1.1.16", "1.1.8"));
    }
}
