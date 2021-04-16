package de.micromata.borgbutler.server.user;

import de.micromata.borgbutler.server.Languages;
import de.micromata.borgbutler.server.RunningMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Contains only one (dummy) user (for desktop version).
 */
public class SingleUserManager extends UserManager {
    private static final String USER_LOCAL_PREF_KEY = "userLocale";
    private static Logger log = LoggerFactory.getLogger(SingleUserManager.class);
    private UserData singleUser;
    private Preferences preferences;

    public SingleUserManager() {
        if (RunningMode.getUserManagement() != RunningMode.UserManagement.SINGLE) {
            throw new IllegalStateException("Can't use SingleUserManager in user management mode '" + RunningMode.getUserManagement()
                    + "'. Only allowed in '" + RunningMode.UserManagement.SINGLE + "'.");
        }
        log.info("Using SingleUserManger as user manager.");
        preferences = Preferences.userRoot().node("de").node("micromata").node("borgbutler");
        singleUser = new UserData();
        singleUser.setUsername("admin");
        singleUser.setAdmin(true);
        load(singleUser);
    }

    public UserData getUser(String id) {
        return singleUser;
    }

    /**
     * Stores only the user's configured locale as preference.
     *
     * @param userData
     * @see Preferences
     */
    @Override
    public void saveUser(UserData userData) {
        Locale locale = userData.getLocale();
        this.singleUser.setLocale(locale);
        String dateFormat = userData.getDateFormat();
        this.singleUser.setDateFormat(dateFormat);
        String lang = Languages.asString(locale);
        if (lang != null) {
            preferences.put(USER_LOCAL_PREF_KEY, lang);
        } else {
            preferences.remove(USER_LOCAL_PREF_KEY);
        }
        try {
            preferences.flush();
        } catch (BackingStoreException ex) {
            log.error("Can't save user locale to preferences: " + ex.getMessage(), ex);
        }
    }

    private void load(UserData userData) {
        String language = preferences.get(USER_LOCAL_PREF_KEY, "en");
        Locale locale = Languages.asLocale(language);
        singleUser.setLocale(locale);
    }
}
