package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.json.borg.Archive;
import de.micromata.borgbutler.json.borg.FilesystemItem;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ArchiveFilelistCacheTest {
    private static Logger log = LoggerFactory.getLogger(ArchiveFilelistCacheTest.class);

    @Test
    void readWriteTest() throws Exception {
        List<FilesystemItem> list = new ArrayList<>();
        for (int i = 0; i < 1000000; i++) {
            list.add(create(i));
        }
        ArchiveFilelistCache cache = new ArchiveFilelistCache(new File("out"));
        BorgRepoConfig repoConfig = new BorgRepoConfig();
        repoConfig.setRepo("repo");
        Archive archive = new Archive();
        set(archive, "archive", "archive-2018-12-10");
        log.info("Saving " + list.size() + " items to out dir.");
        cache.save(repoConfig, archive, list);
        log.info("Saving done.");
        log.info("Loading items from out dir.");
        FilesystemItem[] filesystemItems = cache.load(repoConfig, archive);
        log.info("Loading " + filesystemItems.length + " items done.");
        assertEquals(list.size(), filesystemItems.length);
        for (int i = 0; i < filesystemItems.length; i++) {
            assertEquals(list.get(i).getPath(), filesystemItems[i].getPath());
        }
    }

    private FilesystemItem create(int i) throws Exception {
        FilesystemItem item = new FilesystemItem();
        set(item, "type", "-").set(item, "mode", "drwxr-xr-x")
                .set(item, "user", "kai").set(item, "group", "user")
                .set(item, "path", "/Users/kai/Test" + i + ".java").set(item, "size", 1000);
        return item;
    }

    private ArchiveFilelistCacheTest set(Object obj, String field, Object value) throws Exception {
        Field f1 = obj.getClass().getDeclaredField(field);
        f1.setAccessible(true);
        f1.set(obj, value);
        return this;
    }
}
