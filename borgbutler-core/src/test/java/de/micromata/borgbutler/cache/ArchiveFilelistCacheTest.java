package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.config.BorgRepoConfig;
import de.micromata.borgbutler.data.Archive;
import de.micromata.borgbutler.json.borg.BorgFilesystemItem;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class ArchiveFilelistCacheTest {
    private static Logger log = LoggerFactory.getLogger(ArchiveFilelistCacheTest.class);

    @Test
    void readWriteTest() throws Exception {
        List<BorgFilesystemItem> list = createList(1000000);
        ArchiveFilelistCache cache = new ArchiveFilelistCache(new File("out"), 100);
        cache.removeAllCacheFiles();
        BorgRepoConfig repoConfig = new BorgRepoConfig();
        repoConfig.setRepo("repo");
        Archive archive = createArchive("2018-12-10");
        log.info("Saving " + list.size() + " items to out dir.");
        cache.save(repoConfig, archive, list);
        log.info("Saving done.");
        log.info("Loading items from out dir.");
        List<BorgFilesystemItem> filesystemItems = cache.load(repoConfig, archive);
        log.info("Loading " + filesystemItems.size() + " items done.");
        assertEquals(list.size(), filesystemItems.size());
        Collections.sort(list);
        for (int i = 0; i < filesystemItems.size(); i++) {
            assertEquals(list.get(i).getPath(), filesystemItems.get(i).getPath());
        }
        cache.removeAllCacheFiles();
    }

    @Test
    void readWriteEmptyTest() throws Exception {
        List<BorgFilesystemItem> list = new ArrayList<>();
        ArchiveFilelistCache cache = new ArchiveFilelistCache(new File("out"), 100);
        cache.removeAllCacheFiles();
        BorgRepoConfig repoConfig = new BorgRepoConfig();
        repoConfig.setRepo("repo");
        Archive archive = createArchive("2018-12-09");
        assertNull(cache.load(repoConfig, archive));
        cache.save(repoConfig, archive, list);
        List<BorgFilesystemItem> filesystemItems = cache.load(repoConfig, archive);
        assertNull(cache.load(repoConfig, archive));
        cache.removeAllCacheFiles();
    }

    @Test
    void cleanUpMaximumSizeTest() throws Exception {
        List<BorgFilesystemItem> list = createList(1000000);
        ArchiveFilelistCache cache = new ArchiveFilelistCache(new File("out"), 3);
        cache.removeAllCacheFiles();
        BorgRepoConfig repoConfig = new BorgRepoConfig();
        repoConfig.setRepo("repo");

        long millis = System.currentTimeMillis();

        Archive archive = createArchive("2018-11-20");
        cache.save(repoConfig, archive, list);
        File oldestFile = cache.getFile(repoConfig, archive);
        setLastModificationTime(oldestFile, millis - 10 * 3600000); // Fake lastModifiedTime - 10 h

        archive = createArchive("2018-11-21");
        cache.save(repoConfig, archive, list);
        File newestFile = cache.getFile(repoConfig, archive);
        setLastModificationTime(newestFile, millis - 60000); // Fake lastModifiedTime - 1min

        archive = createArchive("2018-11-22");
        cache.save(repoConfig, archive, list);
        File file = cache.getFile(repoConfig, archive);
        setLastModificationTime(file, millis - 3600000); // Fake lastModifiedTime - 1 hour

        assertTrue(oldestFile.exists());
        assertTrue(newestFile.exists());
        assertTrue(file.exists());

        cache.cleanUp();
        assertFalse(oldestFile.exists());
        assertFalse(file.exists());
        assertTrue(newestFile.exists());
        cache.removeAllCacheFiles();
    }

    @Test
    void cleanUpExpiredTest() throws Exception {
        List<BorgFilesystemItem> list = createList(1000);
        ArchiveFilelistCache cache = new ArchiveFilelistCache(new File("out"), 3);
        cache.removeAllCacheFiles();
        BorgRepoConfig repoConfig = new BorgRepoConfig();
        repoConfig.setRepo("repo");

        long millis = System.currentTimeMillis();

        Archive archive = createArchive("2018-10-20");
        cache.save(repoConfig, archive, list);
        File notExpiredFile = cache.getFile(repoConfig, archive);
        setLastModificationTime(notExpiredFile, millis - 6 * 24 * 3600000); // Fake lastModifiedTime - 10 h

        archive = createArchive("2018-10-21");
        cache.save(repoConfig, archive, list);
        File expiredFile = cache.getFile(repoConfig, archive);
        setLastModificationTime(expiredFile, millis - 8 * 24 * 3600000); // Fake lastModifiedTime - 10 h

        assertTrue(expiredFile.exists());
        assertTrue(notExpiredFile.exists());

        cache.cleanUp();
        assertFalse(expiredFile.exists());
        assertTrue(notExpiredFile.exists());
        cache.removeAllCacheFiles();
    }

    @Test
    void reducePath() {
        List<BorgFilesystemItem> list = new ArrayList<>();
        String[] items = {
                "d home",
                "d home/kai",
                "- home/kai/abc.txt",
                "d home/kai/Documents",
                "- home/kai/Documents/test1.doc",
                "- home/kai/Documents/test2.doc",
                "- home/kai/image.doc",
                "- home/kai/Files/tmp/a.txt",
                "- home/kai/Files/tmp/b.txt",
                "- home/kai/Java.pdf",
                "- home/kai/Movies/a.mov",
                "- home/pete/txt.mov",
                "- home/steve/1/2/3/4/5/6/7/test.txt",
                "- home/test.txt",
                "- home/xaver/1/2/3/4/5/6/7/test.txt",
                "- opt/local/test.txt",
                "- opt/local/test2.txt",
        };
        for (String path : items) {
            BorgFilesystemItem item = new BorgFilesystemItem().setPath(path.substring(2));
            if (path.startsWith("d")) {
                item.setType("d");
            } else {
                item.setType("-");
            }
            list.add(item);
        }
        ArchiveFilelistCache.compactPathes(list);
        ArchiveFilelistCache.expandPathes(list);
        for (int i = 0; i < list.size(); i++) {
            assertEquals(items[i].substring(2), list.get(i).getPath());
        }

    }

    private List<BorgFilesystemItem> createList(int number) throws Exception {
        List<BorgFilesystemItem> list = new ArrayList<>();
        for (int i = 0; i < 1000000; i++) {
            list.add(create(i));
        }
        return list;
    }

    private BorgFilesystemItem create(int i) throws Exception {
        BorgFilesystemItem item = new BorgFilesystemItem();
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
        set(archive, "name", "archive-" + time);
        set(archive, "time", time);
        return archive;
    }

    private void setLastModificationTime(File file, long lastModificationTime) throws IOException {
        Path path = file.toPath();
        FileTime fileTime = FileTime.fromMillis(lastModificationTime);
        Files.setAttribute(path, "lastModifiedTime", fileTime);
    }
}
