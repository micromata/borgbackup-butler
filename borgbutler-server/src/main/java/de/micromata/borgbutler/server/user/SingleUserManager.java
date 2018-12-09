package de.micromata.borgbutler.server.user;

import de.micromata.borgbutler.server.Languages;
import de.micromata.borgbutler.server.RunningMode;
import de.micromata.borgbutler.server.ServerConfigurationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Contains only one (dummy) user (for desktop version).
 */
public class SingleUserManager extends UserManager {
    private static final String USER_LOCAL_PREF_KEY = "userLocale";
    private static Logger log = LoggerFactory.getLogger(SingleUserManager.class);
    private UserData singleUser;

    public SingleUserManager() {
        if (RunningMode.getUserManagement() != RunningMode.UserManagement.SINGLE) {
            throw new IllegalStateException("Can't use SingleUserManager in user management mode '" + RunningMode.getUserManagement()
                    + "'. Only allowed in '" + RunningMode.UserManagement.SINGLE + "'.");
        }
        log.info("Using SingleUserManger as user manager.");
        singleUser = new UserData();
        singleUser.setUsername("admin");
        singleUser.setAdmin(true);
        String language = ServerConfigurationHandler.getInstance().get("userLocale", null);
        Locale locale = Languages.asLocale(language);
        singleUser.setLocale(locale);
        String dateFormat = ServerConfigurationHandler.getInstance().get("userDateFormat", null);
        singleUser.setDateFormat(dateFormat);
    }

    public UserData getUser(String id) {
        return singleUser;
    }

    /**
     * Stores only the user's configured locale.
     *
     * @param userData
     * @see ServerConfigurationHandler#save(String, String)
     */
    @Override
    public void saveUser(UserData userData) {
        Locale locale = userData.getLocale();
        this.singleUser.setLocale(locale);
        String dateFormat = userData.getDateFormat();
        this.singleUser.setDateFormat(dateFormat);
        String lang = Languages.asString(locale);
        ServerConfigurationHandler.getInstance().save("userLocale", lang);
        ServerConfigurationHandler.getInstance().save("userDateFormat", dateFormat);
    }
}
