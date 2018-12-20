package de.micromata.borgbutler;

import de.micromata.borgbutler.json.borg.BorgFilesystemItem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DiffToolTest {
    @Test
    void differencesTest() {
        BorgFilesystemItem i1 = create("etc", true, "drwx------", 0, "2018-11-21");
        BorgFilesystemItem i2 = create("etc", true, "drwx------", 0, "2018-11-21");
        assertTrue(i1.equals(i2));
        i1.setType("-").setMode("drwxrwxrwx").setMtime("2018-11-22");
        assertFalse(i1.equals(i2));
        i1.setDiffItem(i2).buildDifferencesString();
        assertEquals("type:['d'->'-'], mode:['drwx------'->'drwxrwxrwx'], mtime:['2018-11-21'->'2018-11-22']", i1.getDifferences());
    }

    @Test
    void diffToolTest() {
        List<BorgFilesystemItem> l1 = null;
        List<BorgFilesystemItem> l2 = null;
        List<BorgFilesystemItem> result;
        assertEquals(0, DiffTool.extractDifferences(l1, l2).size());
        l1 = create();
        result = DiffTool.extractDifferences(l1, l2);
        assertEquals(7, result.size());
        assertEquals(BorgFilesystemItem.DiffStatus.NEW, result.get(0).getDiffStatus());
        assertEquals(BorgFilesystemItem.DiffStatus.NEW, result.get(1).getDiffStatus());
        result = DiffTool.extractDifferences(l2, l1);
        assertEquals(7, result.size());
        assertEquals(BorgFilesystemItem.DiffStatus.REMOVED, result.get(0).getDiffStatus());
        assertEquals(BorgFilesystemItem.DiffStatus.REMOVED, result.get(1).getDiffStatus());

        l1 = create();
        l2 = create();
        result = DiffTool.extractDifferences(l2, l1);
        assertEquals(0, result.size());
        remove(l2, "etc"); // 0
        remove(l2, "etc/passwd"); // 1
        remove(l1, "home/kai/.borgbutler/borgbutler-config-bak.json"); // 2
        get(l1, "home/kai/.borgbutler/borgbutler-config.json").setSize(712).setMtime("2018-11-22"); // 3
        result = DiffTool.extractDifferences(l1, l2);
        assertEquals(4, result.size());
        assertEquals(BorgFilesystemItem.DiffStatus.NEW, result.get(0).getDiffStatus());
        assertEquals(BorgFilesystemItem.DiffStatus.NEW, result.get(1).getDiffStatus());
        assertEquals(BorgFilesystemItem.DiffStatus.REMOVED, result.get(2).getDiffStatus());
        assertEquals(BorgFilesystemItem.DiffStatus.MODIFIED, result.get(3).getDiffStatus());

        result = DiffTool.extractDifferences(l2, l1);
        assertEquals(4, result.size());
        assertEquals(BorgFilesystemItem.DiffStatus.REMOVED, result.get(0).getDiffStatus());
        assertEquals(BorgFilesystemItem.DiffStatus.REMOVED, result.get(1).getDiffStatus());
        assertEquals(BorgFilesystemItem.DiffStatus.NEW, result.get(2).getDiffStatus());
        assertEquals(BorgFilesystemItem.DiffStatus.MODIFIED, result.get(3).getDiffStatus());

        l1 = create();
        l2 = create();
        remove(l2, "etc"); // 0
        remove(l2, "etc/passwd"); // 1
        remove(l1, "home/kai/.borgbutler/borgbutler-config.json"); // 2
        result = DiffTool.extractDifferences(l1, l2);
        assertEquals(3, result.size());
        assertEquals(BorgFilesystemItem.DiffStatus.NEW, result.get(0).getDiffStatus());
        assertEquals(BorgFilesystemItem.DiffStatus.NEW, result.get(1).getDiffStatus());
        assertEquals(BorgFilesystemItem.DiffStatus.REMOVED, result.get(2).getDiffStatus());
        result = DiffTool.extractDifferences(l2, l1);
        assertEquals(3, result.size());
        assertEquals(BorgFilesystemItem.DiffStatus.REMOVED, result.get(0).getDiffStatus());
        assertEquals(BorgFilesystemItem.DiffStatus.REMOVED, result.get(1).getDiffStatus());
        assertEquals(BorgFilesystemItem.DiffStatus.NEW, result.get(2).getDiffStatus());
    }

    private BorgFilesystemItem create(String path, boolean directory, String mode, long size, String mtime) {
        return new BorgFilesystemItem()
                .setPath(path)
                .setType(directory ? "d" : "-")
                .setMode(mode)
                .setSize(size)
                .setMtime(mtime);
    }

    private List<BorgFilesystemItem> create() {
        List<BorgFilesystemItem> list = new ArrayList<>();
        list.add(create("etc", true, "drwx------", 0, "2018-11-21"));
        list.add(create("etc/passwd", false, "-rwx------", 100, "2018-11-21"));
        list.add(create("home", true, "drwx------", 0, "2018-11-21"));
        list.add(create("home/kai", true, "-rwx------", 0, "2018-11-21"));
        list.add(create("home/kai/.borgbutler", true, "-rwx------", 0, "2018-11-21"));
        list.add(create("home/kai/.borgbutler/borgbutler-config-bak.json", false, "drwxr-xr-x", 666, "2018-11-19"));
        list.add(create("home/kai/.borgbutler/borgbutler-config.json", false, "drwxr-xr-x", 666, "2018-11-21"));
        Collections.sort(list);
        return list;
    }

    private void remove(List<BorgFilesystemItem> list, String path) {
        BorgFilesystemItem item = get(list, path);
        list.remove(item);
    }

    private BorgFilesystemItem get(List<BorgFilesystemItem> list, String path) {
        for (BorgFilesystemItem item : list) {
            if (item.getPath().equals(path)) {
                return item;
            }
        }
        fail();
        return null;
    }
}
