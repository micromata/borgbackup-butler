package de.micromata.borgbutler.cache;

import org.micromata.borgbutler.config.ConfigurationHandler;
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

    public static JCSCache getInstance() {
        return instance;
    }

    private File cacheDir;

    public <K, V> CacheAccess<K, V> getJCSCache(String region) {
        return JCS.getInstance(region);
    }

    private JCSCache() {
        cacheDir = new File(ConfigurationHandler.getInstance().getWorkingDir(), CACHE_DIR_NAME);
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
        //props.setProperty("jcs.auxiliary.DC2.attributes.DiskPath", cacheDir.getAbsolutePath());
        //int cacheMaxDiscSizeMB = configuration.getCacheMaxDiscSizeMB();
        //log.info("Using cache size for archive contents: " + cacheMaxDiscSizeMB + "MB.");
        //props.setProperty("jcs.auxiliary.DC2.attributes.MaxKeySize", String.valueOf(cacheMaxDiscSizeMB * 1000));
        JCS.setConfigProperties(props);
    }

    public File getCacheDir() {
        return this.cacheDir;
    }
}
