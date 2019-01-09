package de.micromata.borgbutler.data;

import de.micromata.borgbutler.json.borg.BorgFilesystemItem;

import java.util.ArrayList;
import java.util.List;

public class FileSystemFilterTest {
    private BorgFilesystemItem create(String path, boolean directory) {
        BorgFilesystemItem item = new BorgFilesystemItem().setPath(path);
        if (directory) {
            item.setType("d");
        } else {
            item.setType("-");
        }
        return item;
    }

    private List<BorgFilesystemItem> createList() {
        List<BorgFilesystemItem> list = new ArrayList<>();
        list.add(create("home", true));
        list.add(create("home/admin", false));
        list.add(create("home/kai", true));
        list.add(create("home/kai/borg/cache", false));
        list.add(create("home/kai/borg/config", false));
        list.add(create("home/kai/Java/test.java", false));
        list.add(create("home/kai/Java/test2.java", false));
        list.add(create("home/.bashrc", false));
        list.add(create("home/.borgbutler", true));
        list.add(create("etc/apache", true));
        list.add(create("etc/apache/http.conf", false));
        return list;
    }
}
