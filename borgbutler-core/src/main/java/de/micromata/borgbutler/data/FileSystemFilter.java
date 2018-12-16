package de.micromata.borgbutler.data;

import de.micromata.borgbutler.json.borg.BorgFilesystemItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

public class FileSystemFilter {
    @Getter
    private String searchString;
    @Getter
    @Setter
    private int maxResultSize;
    private String[] searchKeyWords;

    public boolean matches(BorgFilesystemItem item) {
        if (searchKeyWords == null) {
            return true;
        }
        for (String searchKeyWord : searchKeyWords) {
            if (!StringUtils.containsIgnoreCase(item.getPath(), searchKeyWord))
                return false;
        }
        return true;
    }

    /**
     *
     * @param searchString The search string. If this string contains several key words separated by white chars,
     *                     all key words must be found.
     * @return this for chaining.
     */
    public FileSystemFilter setSearchString(String searchString) {
        this.searchString = searchString;
        searchKeyWords = StringUtils.split(searchString);
        if (searchKeyWords != null && searchKeyWords.length == 0) {
            searchKeyWords = null;
        }
        return this;
    }
}
