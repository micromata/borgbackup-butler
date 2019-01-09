package de.micromata.borgbutler.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import de.micromata.borgbutler.data.FileSystemFilter;
import de.micromata.borgbutler.json.borg.BorgFilesystemItem;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ButlerCacheHelper {
    private static Logger log = LoggerFactory.getLogger(ButlerCacheHelper.class);

    /**
     * Builds the tree of the file items and reduces the path of each item (dependent on the parent path).
     *
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
                    if (current._getInternalDirectory().startsWith(stackObject._getInternalDirectory())) {
                        parent = stackObject;
                        current.setParentFileNumber(parent.getFileNumber());
                        break;
                    }
                    stack.pop();
                }
                if (parent != null) {
                    current.setPath(current.getPath().substring(parent._getInternalDirectory().length() + 1));
                }
                if (current.isDirectory()) {
                    stack.push(current);
                }
            }
        }
    }

    static List<BorgFilesystemItem> readAndMatchList(List<BorgFilesystemItem> sourceList, FileSystemFilter filter) {
        // For storing parent directories:
        Map<Integer, BorgFilesystemItem> directoryMap = new HashMap<>();
        List<BorgFilesystemItem> result = new ArrayList<>();
        Iterator<BorgFilesystemItem> it = sourceList.iterator();
        while (it.hasNext()) {
            BorgFilesystemItem current = it.next();
            setFullPath(directoryMap, current);
            if (filter == null || filter.matches(current)) {
                result.add(current);
                if (filter != null && filter.isFinished()) break;
            }
            if (current.isDirectory()) {
                directoryMap.put(current.getFileNumber(), current);
            }
        }
        if (filter != null) {
            result = filter.reduce(directoryMap, result);
        }
        return result;
    }

    static List<BorgFilesystemItem> readAndMatchInputStream(Kryo kryo, Input input, FileSystemFilter filter) throws IOException {
        // For storing parent directories:
        Map<Integer, BorgFilesystemItem> directoryMap = new HashMap<>();
        List<BorgFilesystemItem> result = new ArrayList<>();
        int size = kryo.readObject(input, Integer.class);
        for (int i = 0; i < size; i++) {
            BorgFilesystemItem current = kryo.readObject(input, BorgFilesystemItem.class);
            setFullPath(directoryMap, current);
            if (filter == null || filter.matches(current)) {
                result.add(current);
                if (filter != null && filter.isFinished()) break;
            }
            if (current.isDirectory()) {
                directoryMap.put(current.getFileNumber(), current);
            }
        }
        if (filter != null) {
            result = filter.reduce(directoryMap, result);
        }
        return result;
    }

    private static void setFullPath(Map<Integer, BorgFilesystemItem> directoryMap, BorgFilesystemItem current) {
        Integer parentFileNumber = current.getParentFileNumber();
        if (parentFileNumber == null) {
            // No parent, nothing to do.
            return;
        }
        BorgFilesystemItem parent = directoryMap.get(parentFileNumber);
        if (parent == null) {
            log.error("Internal error: Oups, parent directory not found in map (wrong processing order?)");
            return;
        }
        if (!parent._isInternalDirectorySet()) {
            setFullPath(directoryMap, parent);
        }
        if (current.isDirectory()) {
            current._setInternalDirectory(FilenameUtils.concat(parent.getFullPath(), current.getPath()));
        } else {
            current._setInternalDirectory(parent.getFullPath() + File.separatorChar);
        }
    }
}
