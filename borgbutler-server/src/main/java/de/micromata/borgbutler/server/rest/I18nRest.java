package de.micromata.borgbutler.server.rest;

import de.micromata.borgbutler.json.JsonUtils;
import de.micromata.borgbutler.server.I18nClientMessages;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Locale;
import java.util.Map;

@Path("/i18n")
public class I18nRest {
    private Logger log = LoggerFactory.getLogger(I18nRest.class);

    /**
     *
     * @param requestContext For detecting the user's client locale.
     * @param locale If not given, the client's language (browser) will be used.
     * @param keysOnly If true, only the keys will be returned. Default is false.
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @see JsonUtils#toJson(Object, boolean)
     */
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public String getList(@Context HttpServletRequest requestContext, @QueryParam("prettyPrinter") boolean prettyPrinter,
                          @QueryParam("keysOnly") boolean keysOnly, @QueryParam("locale") String locale) {
        Locale localeObject;
        if (StringUtils.isNotBlank(locale)) {
            localeObject = new Locale(locale);
        } else {
            localeObject = RestUtils.getUserLocale(requestContext);
        }
        Map<String, String> translations = I18nClientMessages.getInstance().getAllMessages(localeObject, keysOnly);
        String json = JsonUtils.toJson(translations, prettyPrinter);
        return json;
    }
}
