package de.micromata.borgbutler.cache;

/**
 * ,
 * PARSING - , DIRTY - , SAVED -
 */
public enum CacheState {
    /**
     * On startup or directly after {@link AbstractCache#clear()} is called. (not yet read).
     */
    INITIAL,
    /**
     * Cache is been loaded (with borg command).
     */
    LOADING_FROM_BORG,
    /**
     * Cache is beeing loaded from filesystem.
     */
    LOADING_FROM_CACHE_FILE,
    /**
     * Modifications done but not yet written.
     */
    DIRTY,
    /**
     * Save to cache file is in progress.
     */
    SAVING,
    /**
     * Content has been written to file, no modifications done after. Cache file is up-to-date.
     */
    SAVED

}