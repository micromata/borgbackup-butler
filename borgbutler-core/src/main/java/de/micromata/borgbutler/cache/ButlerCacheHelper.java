package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.json.borg.BorgFilesystemItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ButlerCacheHelper {
    private static Logger log = LoggerFactory.getLogger(ButlerCacheHelper.class);

    /**
     * Builds the tree of the file items and reduces the path of each item (dependent on the parent path).
     * @param filesystemItems
     */
    static void proceedBorgFileList(List<BorgFilesystemItem> filesystemItems) {
        // Following algorithm works only if the items are ordered by path:
        Collections.sort(filesystemItems); // Sort by path.
        // For storing parent directories:
        Deque<BorgFilesystemItem> stack = new ArrayDeque<>();
        String parentDirectory;
        int fileNumber = -1;
        Iterator<BorgFilesystemItem> it = filesystemItems.iterator();
        while (it.hasNext()) {
            BorgFilesystemItem current = it.next();
            current.setFileNumber(++fileNumber);
            if (stack.isEmpty()) {
                if (current.isDirectory()) {
                    stack.push(current); // Item has no parent directory.
                } else {
                    log.warn("Oups, unexpected non-directory without parent directory as previous entry: " + current.getPath());
                }
            } else {
                BorgFilesystemItem parent = null;
                while (!stack.isEmpty()) {
                    BorgFilesystemItem stackObject = stack.peek();
                    if (current.getDirectory().startsWith(stackObject.getDirectory())) {
                        parent = stackObject;
                        current.setParentFileNumber(parent.getFileNumber());
                        break;
                    }
                    stack.pop();
                }
                if (parent != null) {
                    current.setPath(current.getPath().substring(parent.getDirectory().length() + 1));
                }
                if (current.isDirectory()) {
                    stack.push(current);
                }
            }
        }
    }

}
