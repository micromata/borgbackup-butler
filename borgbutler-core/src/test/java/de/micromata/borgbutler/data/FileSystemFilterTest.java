package de.micromata.borgbutler.data;

import de.micromata.borgbutler.cache.FilesystemItem;
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
        List<FilesystemItem> list = createList();
        filter.setCurrentDirectory("").setMode(FileSystemFilter.Mode.TREE);
        for (FilesystemItem item : list) {
            if (filter.matches(item)) {
                // Do nothing.
            }
        }
       // list = filter.reduce(list);
        assertEquals(2, list.size());
        list = createList();
        filter.setCurrentDirectory("home");
        for (FilesystemItem item : list) {
            if (filter.matches(item)) {
                // Do nothing.
            }
        }
      //  list = filter.reduce(list);
        assertEquals(4, list.size());
        assertEquals(".bashrc", list.get(0).getDisplayPath());
        assertEquals(".borgbutler", list.get(1).getDisplayPath());

    }

    private FilesystemItem create(String path, boolean directory) {
        FilesystemItem item = new FilesystemItem();
        item.setPath(path);
        if (directory) {
            item.setType("d");
        } else {
            item.setType("-");
        }
        return item;
    }

    private List<FilesystemItem> createList() {
        List<FilesystemItem> list = new ArrayList<>();
        list.add(create("home", true));
        list.add(create("home/.bashrc", false));
        list.add(create("home/.borgbutler", true));
        list.add(create("home/admin", false));
        list.add(create("home/kai", true));
        list.add(create("home/kai/borg/cache", false));
        list.add(create("home/kai/borg/config", false));
        list.add(create("home/kai/Java/test.java", false));
        list.add(create("home/kai/Java/test2.java", false));
        list.add(create("etc/apache", true));
        list.add(create("etc/apache/http.conf", false));
        return list;
    }
}
