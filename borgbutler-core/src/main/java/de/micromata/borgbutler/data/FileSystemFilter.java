package de.micromata.borgbutler.data;

import de.micromata.borgbutler.cache.ButlerCache;
import de.micromata.borgbutler.json.borg.BorgFilesystemItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileSystemFilter {
    private  Logger log = LoggerFactory.getLogger(FileSystemFilter.class);
    public enum Mode {FLAT, TREE}

    @Getter
    private String searchString;
    @Getter
    private Mode mode;
    @Getter
    @Setter
    private String currentDirectory;
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
        if (fileNumber != null) {
            if (item.getFileNumber() == fileNumber) {
                finished = true; // Force finishing.
                return true;
            }
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
                if (matchesDirectoryView(set, item)) {
                    list.add(item);
                }
            }
        }
        return list;
    }

    /**
     * After processing all files with {@link #matches(BorgFilesystemItem)} you should process the file list again
     * through this filter (for tree view) for displaying only the sub items of the current directory (not recursive).
     *
     * @return
     */
    private boolean matchesDirectoryView(Set<String> set, BorgFilesystemItem item) {
        String path = item.getPath();
        if (StringUtils.isEmpty(currentDirectory)) {
            // root dir
            return checkNotYetAbsent(set, path);
        }
        if (!path.startsWith(currentDirectory)) {
            // item is not a child of currentDirectory.
            return false;
        }
        if (path.length() <= currentDirectory.length() + 1) {
            // Don't show the current directory itself.
            return false;
        }
        String subPath = path.substring(currentDirectory.length());
        return checkNotYetAbsent(set, subPath);
    }

    /**
     * It's possible, that borg does return something like this:
     * <ol>
     * <li>home/user/documents</li>
     * <li>home/user/fotos/2018</li>
     * <li>home/user/fotos/2019</li>
     * <li>home/user/movies</li>
     * <li>home/user/movies...</li>
     * </ol>
     * The entry <tt>home/user/fotos</tt> itself is missed (perhaps while the permissions are not given for the backup script.
     * This method ensures, that such directories as fotos will be added.
     * <br>
     * This method only functioned for ordered lists (by path in ascending order).
     *
     * @param set  Already added items.
     * @param path The path of the current item.
     * @return true if the path was absent and was added.
     */
    private boolean checkNotYetAbsent(Set<String> set, String path) {
        int pos = path.indexOf('/');
        if (pos < 0) {
            if (set.contains(path)) {
                log.warn("Shouldn't occur! (Reason: unordered list or bug.)");
                return false;
            }
            set.add(path);
            return true;
        }
        String parent = path.substring(0, pos);
        if (set.contains(parent)) {
            return false;
        }
        set.add(parent);
        return true;
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
            this.mode = Mode.TREE;
        } else {
            this.mode = Mode.FLAT;
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
}
