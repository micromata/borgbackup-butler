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
    // For storing sub directories of the currentDirectory
    private Map<String, BorgFilesystemItem> subDirectories;
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
            // In this run only register all direct childs of currentDirectory.
            String topLevelDir = getTopLevel(item.getPath());
            if (topLevelDir == null) {
                // item is not inside the current directory.
                return false;
            }
            if (!subDirectories.containsKey(topLevelDir)) {
                subDirectories.put(topLevelDir, item);
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

    /**
     * After processing a list by using {@link #matches(BorgFilesystemItem)} you should call finally this method with
     * your result list to reduce the files and directories for mode {@link Mode#TREE}. For the mode {@link Mode#FLAT}
     * nothing is done.
     * <br>
     * Reorders the list (.files will be displayed after normal files).
     *
     * @param list
     * @return The original list for mode {@link Mode#FLAT} or the reduced list for the tree view.
     */
    public List reduce(List<BorgFilesystemItem> list) {
        if (mode == FileSystemFilter.Mode.TREE) {
            Set<String> set = new HashSet<>();
            List<BorgFilesystemItem> list2 = list;
            list = new ArrayList<>();
            for (BorgFilesystemItem item : list2) {
                String topLevel = getTopLevel(item.getPath());
                if (topLevel == null) {
                    continue;
                }
                if (set.contains(topLevel) == false) {
                    set.add(topLevel);
                    BorgFilesystemItem topItem = subDirectories.get(topLevel);
                    topItem.setDisplayPath(StringUtils.removeStart(topItem.getPath(), currentDirectory));
                    list.add(topItem);
                }
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

    /**
     * @param path The path of the current item.
     * @return null if the item is not a child of the current directory otherwise the top level sub directory name of
     * the current directory.
     */
    String getTopLevel(String path) {
        if (StringUtils.isEmpty(currentDirectory)) {
            int pos = path.indexOf('/');
            if (pos < 0) {
                return path;
            }
            return path.substring(0, pos);
        }
        if (!path.startsWith(currentDirectory)) {
            // item is not a child of currentDirectory.
            return null;
        }
        if (path.length() <= currentDirectory.length() + 1) {
            // Don't show the current directory itself.
            return null;
        }
        path = StringUtils.removeStart(path, currentDirectory);
        int pos = path.indexOf('/');
        if (pos < 0) {
            return path;
        }
        return path.substring(0, pos);
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
        if (mode == Mode.TREE) {
            this.subDirectories = new HashMap<>(); // needed only for tree view.
        }
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
        if (currentDirectory != null && currentDirectory.length() > 0 && !currentDirectory.endsWith("/")) {
            this.currentDirectory = currentDirectory + "/";
        } else {
            this.currentDirectory = currentDirectory;
        }
        return this;
    }
}
