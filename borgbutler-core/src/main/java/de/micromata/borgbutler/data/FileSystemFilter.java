package de.micromata.borgbutler.data;

import de.micromata.borgbutler.json.borg.BorgFilesystemItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

public class FileSystemFilter {
    @Getter
    @Setter
    private String searchString;
    @Getter
    @Setter
    private int maxResultSize;

    public boolean matches(BorgFilesystemItem item) {
        if (searchString == null || searchString.length() == 0) {
            return true;
        }
        return StringUtils.containsIgnoreCase(item.getPath(), searchString);
    }
}
