package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.json.borg.Archive;
import de.micromata.borgbutler.json.borg.FilesystemItem;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


public class ArchiveFilelistCacheTest {
    private static Logger log = LoggerFactory.getLogger(ArchiveFilelistCacheTest.class);

    @Test
    void readWriteTest() throws Exception {
        List<FilesystemItem> list = createList(1000000);
        ArchiveFilelistCache cache = new ArchiveFilelistCache(new File("out"), 100);
        cache.removeAllCacheFiles();
        BorgRepoConfig repoConfig = new BorgRepoConfig();
        repoConfig.setRepo("repo");
        Archive archive = createArchive("2018-12-10");
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
        cache.removeAllCacheFiles();
    }

    @Test
    void readWriteEmptyTest() throws Exception {
        List<FilesystemItem> list = new ArrayList<>();
        ArchiveFilelistCache cache = new ArchiveFilelistCache(new File("out"), 100);
        cache.removeAllCacheFiles();
        BorgRepoConfig repoConfig = new BorgRepoConfig();
        repoConfig.setRepo("repo");
        Archive archive = createArchive("2018-12-09");
        assertNull(cache.load(repoConfig, archive));
        cache.save(repoConfig, archive, list);
        FilesystemItem[] filesystemItems = cache.load(repoConfig, archive);
        assertNull(cache.load(repoConfig, archive));
        cache.removeAllCacheFiles();
    }

    @Test
    void cleanUpTest() throws Exception {
        List<FilesystemItem> list = createList(1000000);
        ArchiveFilelistCache cache = new ArchiveFilelistCache(new File("out"), 5);
        cache.removeAllCacheFiles();
        BorgRepoConfig repoConfig = new BorgRepoConfig();
        repoConfig.setRepo("repo");

        long millis = System.currentTimeMillis();

        Archive archive = createArchive("2018-11-20");
        cache.save(repoConfig, archive, list);
        setLastAccessTime(cache.getFile(repoConfig, archive), millis - 10 * 3600000); // Fake lastAccessTime - 10 h

        archive = createArchive("2018-11-21");
        cache.save(repoConfig, archive, list);
        setLastAccessTime(cache.getFile(repoConfig, archive), millis - 60000); // Fake lastAccessTime - 1min

        cache.save(repoConfig, archive, list);
        cache.cleanUp();
        cache.removeAllCacheFiles();
    }

    private List<FilesystemItem> createList(int number) throws Exception {
        List<FilesystemItem> list = new ArrayList<>();
        for (int i = 0; i < 1000000; i++) {
            list.add(create(i));
        }
        return list;
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

    private Archive createArchive(String time) throws Exception {
        Archive archive = new Archive();
        set(archive, "archive", "archive-" + time);
        set(archive, "time", time);
        return archive;
    }

    private void setLastAccessTime(File file, long accessTime) throws IOException {
        Path path = file.toPath();
        FileTime fileTime = FileTime.fromMillis(accessTime);
        Files.setAttribute(path, "lastAccessTime", fileTime);
    }
}
