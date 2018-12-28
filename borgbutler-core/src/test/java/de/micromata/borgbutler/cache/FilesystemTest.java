package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.json.borg.BorgFilesystemItem;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class FilesystemTest {
    private static Logger log = LoggerFactory.getLogger(FilesystemTest.class);

    @Test
    void filesystemTest() {
        List<BorgFilesystemItem> borgList = new ArrayList<>();
        borgList.add(create("home/kai/test.xls"));
        FilesystemItem root = Filesystem.build(borgList);
        List<FilesystemItem> list  = root.getChilds();
        assertEquals(1, list.size());
        assertEquals("home", list.get(0).getName());
        assertEquals("kai", list.get(0).getChilds().get(0).getName());
        assertEquals("test.xls", list.get(0).getChilds().get(0).getChilds().get(0).getName());
        assertEquals("home", root.find("home").getName());
        assertEquals("kai", root.find("home/kai").getName());
    }

    private BorgFilesystemItem create(String path) {
        return new BorgFilesystemItem().setPath(path);
    }
}
