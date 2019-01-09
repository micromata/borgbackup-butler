package de.micromata.borgbutler.data;

import de.micromata.borgbutler.json.borg.BorgFilesystemItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FileSystemFilter {
    private Logger log = LoggerFactory.getLogger(FileSystemFilter.class);

    public enum Mode {FLAT, TREE}

    @Getter
    private String searchString;
    @Getter
    private Mode mode;
    @Getter
    private String currentDirectory;
    private transient Integer currentDirectoryFileNumber;
    @Getter
    @Setter
    private int maxResultSize = -1;
    /**
     * If given, only the file assigned to this number is searched and returned.
     */
    @Getter
    @Setter
    private Integer fileNumber;
    private String[] searchKeyWords;
    private String[] blackListSearchKeyWords;
    private int counter = 0;
    @Getter
    private boolean finished;

    /**
     * Please ensure that you call matches exactly ones for every file item. If matches returns true, the internal
     * item counter is incremented (for maxResultSize functionality).
     * <br>
     * If the number of positive matches is greater than {@link #maxResultSize}, the finished flag is set to true.
     *
     * @param item
     * @return true if the given item matches this filter.
     */
    public boolean matches(BorgFilesystemItem item) {
        item.setDisplayPath(item.getPath());
        if (fileNumber != null) {
            if (item.getFileNumber() == fileNumber) {
                finished = true; // Force finishing.
                return true;
            }
            return false;
        }
        if (mode == Mode.TREE) {
            // Check if this item has the current directory as parent.
            if (!StringUtils.isEmpty(currentDirectory)) {
                if (!item.getFullPath().startsWith(currentDirectory)) {
                    return false;
                }
                if (currentDirectoryFileNumber == null) {
                    // Alphabetical order! Therefore the first matching entry is the top level directory:
                    currentDirectoryFileNumber = item.getFileNumber();
                    if (item.getFullPath().length() - currentDirectory.length() > 1) {
                        log.error("Internal error. Not in alphabetical order?");
                    }
                    return false; // But do not add the current directory itself.
                }
            }
        }
        if (searchKeyWords == null && blackListSearchKeyWords == null) {
            processFinishedFlag();
            return true;
        }
        if (item.getPath() == null) {
            return false;
        }
        String path = item.getPath().toLowerCase();
        if (searchKeyWords != null) {
            for (String searchKeyWord : searchKeyWords) {
                if (!path.contains(searchKeyWord))
                    return false;
            }
        }
        if (blackListSearchKeyWords != null) {
            for (String blackListSearchKeyWord : blackListSearchKeyWords) {
                if (path.contains(blackListSearchKeyWord))
                    return false;
            }
        }
        processFinishedFlag();
        return true;
    }

    public List<BorgFilesystemItem> reduce(Map<Integer, BorgFilesystemItem> directoryMap, List<BorgFilesystemItem> list) {
        if (mode == FileSystemFilter.Mode.TREE) {
            Set<BorgFilesystemItem> set = new HashSet<>();
            List<BorgFilesystemItem> list2 = list;
            list = new ArrayList<>();
            for (BorgFilesystemItem item : list2) {
                BorgFilesystemItem topItem = findTopLevel(directoryMap, item, currentDirectoryFileNumber);
                if (topItem == null) {
                    continue;
                }
                if (set.contains(topItem)) {
                    // Already added.
                    continue;
                }
                list.add(topItem);
                set.add(topItem);
            }
            list2 = list;
            // Re-ordering (show dot files at last)
            list = new ArrayList<>();
            // First add normal files:
            for (BorgFilesystemItem item : list2) {
                if (!item.getDisplayPath().startsWith(".")) {
                    list.add(item);
                }
            }
            // Now add dot files:
            for (BorgFilesystemItem item : list2) {
                if (item.getDisplayPath().startsWith(".")) {
                    list.add(item);
                }
            }
        }
        return list;
    }

    private BorgFilesystemItem findTopLevel(Map<Integer, BorgFilesystemItem> directoryMap, BorgFilesystemItem item,
                                            Integer currentDirectoryFileNumber) {
        Integer parentFileNumber = item.getParentFileNumber();
        if (Objects.equals(parentFileNumber, currentDirectoryFileNumber)) {
            // parent object is the current directory, found.
            return item;
        }
        if (parentFileNumber == null) {
            log.error("Internal error: couldn't find current directory as parent directory! Ignoring: " + item.getFullPath());
        }
        BorgFilesystemItem parent = directoryMap.get(item.getParentFileNumber());
        if (parent == null) {
            log.error("Internal error: couldn't find current directory as parent directory! Ignoring: " + item.getFullPath());
            return null;
        }
        return findTopLevel(directoryMap, parent, currentDirectoryFileNumber);
    }


    /**
     * @param searchString The search string. If this string contains several key words separated by white chars,
     *                     all key words must be found.
     * @return this for chaining.
     */
    public FileSystemFilter setSearchString(String searchString) {
        this.searchString = searchString;
        String[] keyWords = StringUtils.split(searchString);
        this.searchKeyWords = null;
        this.blackListSearchKeyWords = null;
        if (keyWords != null) {
            List<String> whiteList = new ArrayList<>();
            List<String> blackList = new ArrayList<>();
            for (String keyWord : keyWords) {
                if (StringUtils.isEmpty(keyWord)) {
                    continue;
                }
                if (keyWord.startsWith("!") && keyWord.length() > 1) {
                    blackList.add(keyWord.substring(1).toLowerCase());
                } else {
                    whiteList.add(keyWord.toLowerCase());
                }
            }
            if (whiteList.size() > 0) {
                this.searchKeyWords = new String[whiteList.size()];
                this.searchKeyWords = whiteList.toArray(this.searchKeyWords);
            }
            if (blackList.size() > 0) {
                this.blackListSearchKeyWords = new String[blackList.size()];
                this.blackListSearchKeyWords = blackList.toArray(this.blackListSearchKeyWords);
            }
        }
        return this;
    }

    /**
     * @param mode
     * @return this for chaining.
     */
    public FileSystemFilter setMode(String mode) {
        if (mode != null && mode.toLowerCase().equals("tree")) {
            return setMode(Mode.TREE);
        }
        return setMode(Mode.FLAT);
    }

    /**
     * @param mode
     * @return this for chaining.
     */
    public FileSystemFilter setMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    private void processFinishedFlag() {
        if (mode == Mode.TREE) {
            return;
        }
        if (maxResultSize > 0 && ++counter >= maxResultSize) {
            this.finished = true;
        }
    }

    public FileSystemFilter setCurrentDirectory(String currentDirectory) {
        if (currentDirectory != null && currentDirectory.length() > 0 && currentDirectory.endsWith("/")) {
            this.currentDirectory = currentDirectory.substring(0, currentDirectory.length() - 2);
        } else {
            this.currentDirectory = currentDirectory;
        }
        return this;
    }
}
