package de.micromata.borgbutler.server.rest;

import de.micromata.borgbutler.cache.ButlerCache;
import org.micromata.borgbutler.config.ConfigurationHandler;
import de.micromata.borgbutler.json.JsonUtils;
import de.micromata.borgbutler.server.BorgInstallation;
import de.micromata.borgbutler.server.ServerConfiguration;
import de.micromata.borgbutler.server.user.UserData;
import de.micromata.borgbutler.server.user.UserManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/configuration")
public class ConfigurationRest {
    private Logger log = LoggerFactory.getLogger(ConfigurationRest.class);

    /**
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @see JsonUtils#toJson(Object, boolean)
     */
    @GET
    @Path("config")
    @Produces(MediaType.APPLICATION_JSON)
    public String getConfig(@QueryParam("prettyPrinter") boolean prettyPrinter) {
        ConfigurationInfo configurationInfo = new ConfigurationInfo();
        configurationInfo.setServerConfiguration(ServerConfiguration.get());
        configurationInfo.setBorgVersion(BorgInstallation.getInstance().getBorgVersion());
        String json = JsonUtils.toJson(configurationInfo, prettyPrinter);
        return json;
    }

    @POST
    @Path("config")
    @Produces(MediaType.TEXT_PLAIN)
    public void setConfig(String jsonConfig) {
        ConfigurationHandler configurationHandler = ConfigurationHandler.getInstance();
        ConfigurationInfo configurationInfo = JsonUtils.fromJson(ConfigurationInfo.class, jsonConfig);
        BorgInstallation.getInstance().configure(configurationInfo.getServerConfiguration(), configurationInfo.getBorgVersion().getBorgBinary());
        ServerConfiguration configuration = ServerConfiguration.get();
        configuration.copyFrom(configurationInfo.getServerConfiguration());
        configurationHandler.save();
    }

    /**
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @see JsonUtils#toJson(Object, boolean)
     */
    @GET
    @Path("user")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUser(@QueryParam("prettyPrinter") boolean prettyPrinter) {
        UserData user = RestUtils.getUser();
        String json = JsonUtils.toJson(user, prettyPrinter);
        return json;
    }

    @POST
    @Path("user")
    @Produces(MediaType.TEXT_PLAIN)
    public void setUser(String jsonConfig) {
        UserData user = JsonUtils.fromJson(UserData.class, jsonConfig);
        if (user.getLocale() != null && StringUtils.isBlank(user.getLocale().getLanguage())) {
            // Don't set locale with "" as language.
            user.setLocale(null);
        }
        if (StringUtils.isBlank(user.getDateFormat())) {
            // Don't set dateFormat as "".
            user.setDateFormat(null);
        }
        UserManager.instance().saveUser(user);
    }

    /**
     * Resets the settings to default values (deletes all settings).
     */
    @GET
    @Path("clearAllCaches")
    @Produces(MediaType.APPLICATION_JSON)
    public String clearAllCaches() {
        log.info("Clear all caches called...");
        ButlerCache.getInstance().clearAllCaches();
        return "OK";
    }
}
