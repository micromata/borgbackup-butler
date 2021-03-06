package de.micromata.borgbutler.data;

import de.micromata.borgbutler.json.borg.BorgFilesystemItem;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FileSystemFilter {
    private Logger log = LoggerFactory.getLogger(FileSystemFilter.class);

    public String getSearchString() {
        return this.searchString;
    }

    public Mode getMode() {
        return this.mode;
    }

    public boolean isAutoChangeDirectoryToLeafItem() {
        return this.autoChangeDirectoryToLeafItem;
    }

    public String getCurrentDirectory() {
        return this.currentDirectory;
    }

    public int getMaxResultSize() {
        return this.maxResultSize;
    }

    public Integer getFileNumber() {
        return this.fileNumber;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public FileSystemFilter setAutoChangeDirectoryToLeafItem(boolean autoChangeDirectoryToLeafItem) {
        this.autoChangeDirectoryToLeafItem = autoChangeDirectoryToLeafItem;
        return this;
    }

    public FileSystemFilter setMaxResultSize(int maxResultSize) {
        this.maxResultSize = maxResultSize;
        return this;
    }

    public FileSystemFilter setFileNumber(Integer fileNumber) {
        this.fileNumber = fileNumber;
        return this;
    }

    public enum Mode {FLAT, TREE}

    private String searchString;
    private Mode mode;
    /**
     * If true (default): Step in tree view automatically recursively into sub directory if only one sub directory exists in
     * current directory. If false, also a single directory of the current directory is displayed.<br>
     * Has no effect in flat mode.
     */
    private boolean autoChangeDirectoryToLeafItem = true;
    private String currentDirectory;
    // For storing sub directories of the currentDirectory
    private Map<String, BorgFilesystemItem> subDirectories;
    private int maxResultSize = -1;
    /**
     * If given, only the file assigned to this number is searched and returned.
     */
    private Integer fileNumber;
    private String[] searchKeyWords;
    private String[] blackListSearchKeyWords;
    private int counter = 0;
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
        if (!checkDirectoryMatchAndRegisterSubDirectories(item)) {
            return false;
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
     * @param origList
     * @return The original list for mode {@link Mode#FLAT} or the reduced list for the tree view.
     */
    public List<BorgFilesystemItem> reduce(List<BorgFilesystemItem> origList) {
        if (mode != FileSystemFilter.Mode.TREE) {
            return origList;
        }
        Set<String> set = new HashSet<>();
        List<BorgFilesystemItem> list2 = origList;
        List<BorgFilesystemItem> list = new ArrayList<>();
        for (BorgFilesystemItem item : list2) {
            String topLevel = getTopLevel(item.getPath());
            if (topLevel == null) {
                continue;
            }
            if (!set.contains(topLevel)) {
                set.add(topLevel);
                BorgFilesystemItem topItem = subDirectories.get(topLevel);
                if (topItem == null) {
                    log.error("Internal error, can't find subDirectory: " + topLevel);
                } else {
                    BorgFilesystemItem cloneItem = autoChangeDirectoryToLeafItem ? topItem.clone() : topItem;
                    cloneItem.setDisplayPath(StringUtils.removeStart(topItem.getPath(), currentDirectory));
                    list.add(cloneItem);
                }
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
        if (autoChangeDirectoryToLeafItem && list.size() == 1 && "d".equals(list.get(0).getType())) {
            // Only one sub directory is displayed, so change directory automatically to this sub directory:
            FileSystemFilter filter = this.clone();
            filter.setCurrentDirectory(list.get(0).getPath());
            for (BorgFilesystemItem item : origList) {
                filter.matches(item);
            }
            List<BorgFilesystemItem> result =
                    filter.reduce(origList);
            if (CollectionUtils.isNotEmpty(result)) {
                // Use only result, if childs in the current directory do exist.
                return result;
            }
        }
        return list;
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
     * This method has only effect in FLAT view. This method has to be called for every item of the list before
     * {@link #reduce(List)} may work, because this method registers sub directories of the current directory needed
     * by {@link #reduce(List)}.
     *
     * @param item
     * @return false, if the given item is not a sub item of the current directory. You may skip further checkings for this
     * item. If true, this item might be part of the result.
     */
    protected boolean checkDirectoryMatchAndRegisterSubDirectories(BorgFilesystemItem item) {
        if (mode != Mode.TREE) {
            return true;
        }
        if (StringUtils.isNotEmpty(currentDirectory) && !item.getPath().startsWith(currentDirectory)) {
            // item is not inside the current directory.
            return false;
        }
        // In this run only register all direct childs of currentDirectory.
        String topLevelDir = getTopLevel(item.getPath());
        if (topLevelDir == null) {
            // item is not inside the current directory.
            return false;
        }
        if (!subDirectories.containsKey(topLevelDir)) {
            if (!item.getPath().endsWith(topLevelDir)) {
                String currentDir = this.currentDirectory;
                // Mount point? Top level was not received from Borg as separate item. Create a synthetic one (without file number):
                BorgFilesystemItem syntheticItem = new BorgFilesystemItem()
                        .setPath(currentDir + topLevelDir)
                        .setDisplayPath(topLevelDir)
                        .setType("d");
                // TODO: Register synthetic sub directories if exist (see failure of FileSystemFilterTest).
                subDirectories.put(topLevelDir, syntheticItem);
            } else {
                subDirectories.put(topLevelDir, item);
            }
        }
        return true;
    }


    /**
     * currentDirectory '': <tt>home</tt> -&gt; <tt>home</tt><br>
     * currentDirectory '': <tt>home/kai</tt> -&gt; <tt>home</tt><br>
     * currentDirectory 'home': <tt>home</tt> -&gt; <tt>null</tt><br>
     * currentDirectory 'home': <tt>home/kai</tt> -&gt; <tt>kai</tt><br>
     * currentDirectory 'home': <tt>home/kai/test.java</tt> -&gt; <tt>kai</tt><br>
     *
     * @param path The path of the current item.
     * @return null if the item is not a child of the current directory otherwise the top level sub directory name of
     * the current directory.
     */
    String getTopLevel(String path) {
        return getTopLevel(this.currentDirectory, path);
    }

    String getTopLevel(String currentDir, String path) {
        if (StringUtils.isEmpty(currentDir)) {
            int pos = path.indexOf('/');
            if (pos < 0) {
                return path;
            }
            return path.substring(0, pos);
        }
        if (!path.startsWith(currentDir)) {
            // item is not a child of currentDirectory.
            return null;
        }
        if (path.length() <= currentDir.length() + 1) {
            // Don't show the current directory itself.
            return null;
        }
        path = StringUtils.removeStart(path, currentDir);
        int pos = path.indexOf('/');
        if (pos < 0) {
            return path;
        }
        return path.substring(0, pos);
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
        this.currentDirectory = ensureTrailingSeparator(currentDirectory);
        return this;
    }

    private String ensureTrailingSeparator(String dir) {
        if (dir != null && dir.length() > 0 && !dir.endsWith("/")) {
            return dir + "/";
        }
        return dir;
    }

    protected FileSystemFilter clone() {
        FileSystemFilter filter = new FileSystemFilter();
        filter.currentDirectory = this.currentDirectory;
        filter.autoChangeDirectoryToLeafItem = this.autoChangeDirectoryToLeafItem;
        filter.setMode(this.mode);
        filter.searchString = this.searchString;
        filter.maxResultSize = this.maxResultSize;
        filter.fileNumber = this.fileNumber;
        filter.searchKeyWords = this.searchKeyWords;
        filter.blackListSearchKeyWords = this.blackListSearchKeyWords;
        return filter;
    }
}
