package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.data.FileSystemFilter;
import de.micromata.borgbutler.json.borg.BorgFilesystemItem;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class ButlerCacheHelperTest {
    private static Logger log = LoggerFactory.getLogger(ButlerCacheHelperTest.class);

    @Test
    void proceedBorgFileListTest() {
        List<BorgFilesystemItem> list = createList();
        checkList(list);
        String previousPath = null;
        for (int i = 0; i < list.size(); i++) {
            BorgFilesystemItem item = list.get(i);
            if (previousPath != null) {
                // Check order:
                assertTrue((previousPath.compareToIgnoreCase(item.getFullPath()) < 0),
                        "Order expected: " + previousPath + " < " + item.getPath());
            }
            assertEquals(i, (int) item.getFileNumber());
            previousPath = item.getFullPath();
            item._setInternalDirectory(null); // Delete for testing recovery below.
        }
        // Recover full path:
        ButlerCacheHelper.readAndMatchList(list, null);
        checkList(list);
    }

    @Test
    void treeViewTest() {
        List<BorgFilesystemItem> list = createList();
        list = ButlerCacheHelper.readAndMatchList(list, new FileSystemFilter().setMode(FileSystemFilter.Mode.TREE));
        assertEquals(3, list.size());
        assertEquals("home", list.get(0).getFullPath());
        assertEquals("opt", list.get(1).getFullPath());
        assertEquals("test.txt", list.get(2).getFullPath());
        list = createList();
        list = ButlerCacheHelper.readAndMatchList(list, new FileSystemFilter().setCurrentDirectory("home").setMode(FileSystemFilter.Mode.TREE));
        assertEquals(5, list.size());
        assertEquals("home/kai", list.get(0).getFullPath());
        assertEquals("home/pete", list.get(1).getFullPath());
        assertEquals("home/steve/1/2/3/4/5/6/7/test.txt", list.get(2).getFullPath());
        assertEquals("home/test.txt", list.get(3).getFullPath());
        assertEquals("home/xaver/1/2/3/4/5/6/7/test.txt", list.get(4).getFullPath());
        list = createList();
        list = ButlerCacheHelper.readAndMatchList(list, new FileSystemFilter().setCurrentDirectory("home/kai/").setMode(FileSystemFilter.Mode.TREE));
        assertEquals(7, list.size());
        assertEquals("home/kai/abc.txt", list.get(0).getFullPath());
        assertEquals("home/kai/Documents", list.get(1).getFullPath());
        assertEquals("home/kai/Files", list.get(2).getFullPath());
        assertEquals("home/kai/image.doc", list.get(3).getFullPath());
        assertEquals("home/kai/Java.pdf", list.get(4).getFullPath());
        assertEquals("home/kai/Movies/a.mov", list.get(5).getFullPath());
        assertEquals("home/kai/.borgbutler", list.get(6).getFullPath());
    }

    private List<BorgFilesystemItem> createList() {
        List<BorgFilesystemItem> list = new ArrayList<>();
        String[] items = {
                "d home",
                "d home/kai",
                "d home/kai/Documents",
                "d home/kai/Files",
                "d home/kai/Files/tmp",
                "d home/pete",
                "d home/pete/Documents",
                "d home/pete/Movies",
                "d opt",
                "d opt/local",
                "- home/kai/.borgbutler",
                "- home/kai/abc.txt",
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
                "- test.txt"
        };
        for (String path : items) {
            BorgFilesystemItem item = new BorgFilesystemItem().setPath(path.substring(2));
            item.setType(path.startsWith("d") ? "d" : "-");
            list.add(item);
        }
        ButlerCacheHelper.proceedBorgFileList(list);
        return list;
    }

    private void checkList(List<BorgFilesystemItem> list) {
        check(list, "home", null, "home");
        check(list, "home/kai", "home", "kai");
        check(list, "home/kai/Documents", "home/kai", "Documents");
        check(list, "home/kai/Documents/test1.doc", "home/kai/Documents", "test1.doc");
        check(list, "home/kai/Files/tmp/b.txt", "home/kai/Files/tmp", "b.txt");
        check(list, "home/kai/image.doc", "home/kai", "image.doc");
        check(list, "home/pete/Movies", "home/pete", "Movies");
        check(list, "opt", null, "opt");
        check(list, "opt/local", "opt", "local");
        check(list, "opt/local/test.txt", "opt/local", "test.txt");
        check(list, "test.txt", null, "test.txt");
    }

    private void check(List<BorgFilesystemItem> list, String path, String expectedParent, String expectedPath) {
        BorgFilesystemItem item = getItem(list, path);
        if (expectedParent == null) {
            assertNull(item.getParentFileNumber());
        } else {
            assertEquals((int) getItem(list, expectedParent).getFileNumber(), (int) item.getParentFileNumber());
        }
        assertEquals(expectedPath, item.getPath());
    }

    private BorgFilesystemItem getItem(List<BorgFilesystemItem> list, String path) {
        for (BorgFilesystemItem item : list) {
            if (item.isDirectory()) {
                if (path.equals(item._getInternalDirectory())) return item;
            } else {
                String fullPath = item.getFullPath();
                if (path.equals(fullPath)) {
                    return item;
                }
            }
        }
        fail("'" + path + "' not found.");
        return null;
    }
}
