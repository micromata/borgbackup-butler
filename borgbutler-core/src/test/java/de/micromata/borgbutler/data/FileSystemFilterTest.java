package de.micromata.borgbutler.data;

import de.micromata.borgbutler.json.borg.BorgFilesystemItem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FileSystemFilterTest {
    @Test
    void getTopLevelTest() {
        FileSystemFilter filter = new FileSystemFilter();
        assertEquals("home", filter.getTopLevel("home"));
        assertEquals("home", filter.getTopLevel("home/test"));
        assertEquals("home", filter.getTopLevel("home/"));
        filter.setCurrentDirectory("home");
        assertNull(filter.getTopLevel("home"));
        assertEquals("kai", filter.getTopLevel("home/kai"));
        assertEquals("kai", filter.getTopLevel("home/kai/test.java"));
        assertNull(filter.getTopLevel("etc/test"));
        List<BorgFilesystemItem> list = createList();
        filter.setCurrentDirectory("").setMode(FileSystemFilter.Mode.TREE);
        for (BorgFilesystemItem item : list) {
            if (filter.matches(item)) {
                // Do nothing.
            }
        }
        list = filter.reduce(list);
        assertEquals(3, list.size());
        assertEquals("home", list.get(0).getDisplayPath());
        assertEquals("etc", list.get(1).getDisplayPath());
        assertEquals("opt", list.get(2).getDisplayPath());
        assertEquals("opt", list.get(2).getPath());
        assertEquals(-1, list.get(2).getFileNumber());
        assertEquals("d", list.get(2).getType());
        assertNull(list.get(2).getMode()); // Synthetic file item.

        list = createList();
        filter.setCurrentDirectory("home");
        for (BorgFilesystemItem item : list) {
            if (filter.matches(item)) {
                // Do nothing.
            }
        }
        list = filter.reduce(list);
        assertEquals(4, list.size());
        assertEquals("admin", list.get(0).getDisplayPath());
        assertEquals("drwxr-xr-x", list.get(0).getMode());
        assertEquals("kai", list.get(1).getDisplayPath());
        assertEquals("drwxr-xr-x", list.get(1).getMode());
        assertEquals(".bashrc", list.get(2).getDisplayPath());
        assertEquals("-rw-r--r--", list.get(2).getMode());
        assertEquals(".borgbutler", list.get(3).getDisplayPath());

        // Check synthetic items (opt as parent is not an own entry):
        list = createList();
        filter.setCurrentDirectory("opt");
        for (BorgFilesystemItem item : list) {
            if (filter.matches(item)) {
                // Do nothing.
            }
        }
        list = filter.reduce(list);
        assertEquals(2, list.size());
        assertEquals("openhab", list.get(0).getDisplayPath());
        assertEquals("vbox-backups", list.get(1).getDisplayPath());

        // Check auto cd into single sub directories:
        list = createList();
        filter.setCurrentDirectory("home/admin");
        for (BorgFilesystemItem item : list) {
            if (filter.matches(item)) {
                // Do nothing.
            }
        }
        list = filter.reduce(list);
        assertEquals(1, list.size());
        //assertEquals("Documents", list.get(0).getDisplayPath());
        assertEquals("test.txt", list.get(0).getDisplayPath()); // if Filter#autoChangeDirectoryToLeafItem == true works
    }

    private BorgFilesystemItem create(String path, boolean directory) {
        BorgFilesystemItem item = new BorgFilesystemItem().setPath(path);
        if (directory) {
            item.setType("d")
                    .setMode("drwxr-xr-x");
        } else {
            item.setType("-")
                    .setMode("-rw-r--r--");
        }
        return item;
    }

    private List<BorgFilesystemItem> createList() {
        List<BorgFilesystemItem> list = new ArrayList<>();
        list.add(create("home", true));
        list.add(create("home/admin", true));
        list.add(create("home/admin/Documents/www/home/test.txt", false));
        list.add(create("home/kai", true));
        list.add(create("home/kai/borg/cache", false));
        list.add(create("home/kai/borg/config", false));
        list.add(create("home/kai/Java/test.java", false));
        list.add(create("home/kai/Java/test2.java", false));
        list.add(create("home/.bashrc", false));
        list.add(create("home/.borgbutler", true));
        list.add(create("etc/apache", true));
        list.add(create("etc/apache/http.conf", false));
        // opt is not given by borg, because opt is in this example a mount point.
        list.add(create("opt/openhab", true));
        list.add(create("opt/openhab/addons", true));
        list.add(create("opt/openhab/conf", true));
        list.add(create("opt/vbox-backups", true));
        list.add(create("opt/vbox-backups/Oracle_VM_VirtualBox_Extension_Pack-4.3.32-103443.vbox-extpack", false));
        return list;
    }
}
