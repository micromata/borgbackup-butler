package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.json.borg.BorgFilesystemItem;
import org.apache.commons.collections4.CollectionUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class Filesystem {

    public static FilesystemItem build(List<BorgFilesystemItem> itemList) {
        FilesystemItem root = new FilesystemItem();
        if (CollectionUtils.isEmpty(itemList)) {
            return root;
        }
        Collections.sort(itemList);
        for (BorgFilesystemItem item : itemList) {
            Path path = Paths.get(item.getPath());
            root.add(path, 0, item);
        }
        return root;
    }
}
