package de.micromata.borgbutler.data;

import de.micromata.borgbutler.cache.FilesystemItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class FileSystemFilter {
    private Logger log = LoggerFactory.getLogger(FileSystemFilter.class);

    public enum Mode {FLAT, TREE}

    @Getter
    private String searchString;
    @Getter
    private Mode mode;
    @Getter
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

    public List<FilesystemItem> buildMatchList(FilesystemItem root) {
        FilesystemItem current = root;
        if (StringUtils.isNotBlank(currentDirectory)) {
            current = root.find(currentDirectory);
            if (current == null) {
                current = root;
                log.warn("Directory '" + currentDirectory + "' not found. Searching in root directory.");
                currentDirectory = null;
            }
        }
        List<FilesystemItem> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(current.getChilds())) {
            for (FilesystemItem item : current.getChilds()) {
                buildMatchList(item, resultList);
            }
        }
        return resultList;
    }

    private void buildMatchList(FilesystemItem item, List<FilesystemItem> resultList) {
        String pathString = item.getPath();
        if (mode == Mode.TREE && currentDirectory != null) {
            if (matchesRecursive(item)) {
                resultList.add(item);
            }
            return;
        }
        if (matches(item)) {
            resultList.add(item);
            if (CollectionUtils.isEmpty(item.getChilds()))
                return;
            for (FilesystemItem child : item.getChilds()) {
                buildMatchList(child, resultList);
            }
        }
    }

    /**
     * Please ensure that you call matches exactly ones for every file item. If matches returns true, the internal
     * item counter is incremented (for maxResultSize functionality).
     * <br>
     * If the number of positive matches is greater than {@link #maxResultSize}, the finished flag is set to true.
     *
     * @param item
     * @return true if the given item matches this filter.
     */
    public boolean matches(FilesystemItem item) {
        item.setDisplayPath(item.getPath());
        if (fileNumber != null) {
            if (item.getFileNumber() == fileNumber) {
                finished = true; // Force finishing.
                return true;
            }
            return false;
        }
        if (item.getPath() == null) {
            return false;
        }
        if (mode == Mode.TREE) {
            if (currentDirectory != null && item.getPath().startsWith(currentDirectory) == false) {
                return false;
            }
        }
        if (searchKeyWords == null && blackListSearchKeyWords == null) {
            processFinishedFlag();
            return true;
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
     * Please ensure that you call matches exactly ones for every file item. If matches returns true, the internal
     * item counter is incremented (for maxResultSize functionality).
     * <br>
     * If the number of positive matches is greater than {@link #maxResultSize}, the finished flag is set to true.
     *
     * @param item
     * @return true if the given item matches this filter.
     */
    public boolean matchesRecursive(FilesystemItem item) {
        boolean matches = matches(item);
        if (matches == true) {
            return true;
        }
        if (item.getChilds() != null) {
            for (FilesystemItem child : item.getChilds()) {
                if (matchesRecursive(child)) {
                    return true;
                }
            }
        }
        return false;
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
        if (currentDirectory != null && currentDirectory.length() > 0 && !currentDirectory.endsWith("/")) {
            this.currentDirectory = currentDirectory + "/";
        } else {
            this.currentDirectory = currentDirectory;
        }
        return this;
    }
}
