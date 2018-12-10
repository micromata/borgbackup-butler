package de.micromata.borgbutler.cache;

import de.micromata.borgbutler.config.Configuration;
import de.micromata.borgbutler.config.ConfigurationHandler;
import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JCSCache {
    private static Logger log = LoggerFactory.getLogger(JCSCache.class);
    private static JCSCache instance = new JCSCache();
    private static final String CONFIG_FILE = "jcs-basic-config.properties";
    public static final String CACHE_DIR_NAME = "cache";

    public enum Region {DEFAULT, ARCHIVE_CONTENT}

    public static JCSCache getInstance() {
        return instance;
    }

    /**
     * @param <K>
     * @param <V>
     * @return JCS cache for default region.
     */
    public <K, V> CacheAccess<K, V> getJCSCache() {
        return JCS.getInstance("default");
    }

    public <K, V> CacheAccess<K, V> getJCSCache(Region region) {
        switch (region) {
            case ARCHIVE_CONTENT:
                return JCS.getInstance("default");
            default:
                return getJCSCache();
        }
    }

    private JCSCache() {
        Configuration configuration = ConfigurationHandler.getConfiguration();
        File cacheDir = new File(ConfigurationHandler.getInstance().getWorkingDir(), CACHE_DIR_NAME);
        if (!cacheDir.exists()) {
            log.info("Creating cache dir: " + cacheDir.getAbsolutePath());
            cacheDir.mkdir();
        }

        Properties props = new Properties();
        try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(CONFIG_FILE)) {
            props.load(inputStream);
        } catch (IOException ex) {
            log.error("Error while loading jcs config file '" + CONFIG_FILE + "': " + ex.getMessage(), ex);
        }
        props.setProperty("jcs.auxiliary.DC.attributes.DiskPath", cacheDir.getAbsolutePath());
        props.setProperty("jcs.auxiliary.DC2.attributes.DiskPath", cacheDir.getAbsolutePath());
        int cacheMaxDiscSizeMB = configuration.getCacheMaxDiscSizeMB();
        props.setProperty("jcs.auxiliary.DC2.attributes.MaxKeySize", String.valueOf(cacheMaxDiscSizeMB * 1000));
        JCS.setConfigProperties(props);
    }
}
