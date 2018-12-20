package de.micromata.borgbutler;

import de.micromata.borgbutler.json.borg.BorgFilesystemItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Extracts the differences between two archives of one repo.
 */
public class DiffTool {
    private static Logger log = LoggerFactory.getLogger(DiffTool.class);

    /**
     * @param items      Sorted list of items from the current archive.
     * @param otherItems Sorted list of items of the archive to extract differences.
     * @return A list of differing items (new, removed and modified ones).
     */
    public static List<BorgFilesystemItem> extractDifferences(List<BorgFilesystemItem> items, List<BorgFilesystemItem> otherItems) {
        List<BorgFilesystemItem> currentList = items != null ? items : new ArrayList<>();
        List<BorgFilesystemItem> otherList = otherItems != null ? otherItems : new ArrayList<>();
        List<BorgFilesystemItem> result = new ArrayList<>();
        Iterator<BorgFilesystemItem> currentIt = currentList.iterator();
        Iterator<BorgFilesystemItem> otherIt = otherList.iterator();
        BorgFilesystemItem current = null;
        BorgFilesystemItem other = null;
        while (true) {
            if (current == null && currentIt.hasNext())
                current = currentIt.next();
            if (other == null && otherIt.hasNext())
                other = otherIt.next();
            if (current == null || other == null) {
                break;
            }
            int cmp = current.compareTo(other);
            if (cmp == 0) { // Items represents both the same file system item.
                if (current.equals(other)) {
                    current = other = null; // increment both iterators.
                    continue;
                }
                // Current entry differs:
                current.setDiffStatus(BorgFilesystemItem.DiffStatus.MODIFIED);
                current.setDiffItem(other);
                current.buildDifferencesString();
                result.add(current);
                current = other = null; // increment both iterators.
            } else if (cmp < 0) {
                result.add(current.setDiffStatus(BorgFilesystemItem.DiffStatus.NEW));
                current = currentIt.hasNext() ? currentIt.next() : null;
            } else {
                result.add(other.setDiffStatus(BorgFilesystemItem.DiffStatus.REMOVED));
                other = otherIt.hasNext() ? otherIt.next() : null;
            }
        }
        while (currentIt.hasNext()) {
            if (current == null)
                current = currentIt.next();
            result.add(current.setDiffStatus(BorgFilesystemItem.DiffStatus.NEW));
            current = null;
        }
        while (otherIt.hasNext()) {
            if (other == null)
                other = otherIt.next();
            result.add(other.setDiffStatus(BorgFilesystemItem.DiffStatus.REMOVED));
            other = null;
        }
        return result;
    }
}
