package de.micromata.borgbutler.server.rest

import de.micromata.borgbutler.json.JsonUtils
import de.micromata.borgbutler.server.I18nClientMessages
import org.apache.commons.lang3.StringUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/rest/i18n")
class I18nRest {

    /**
     *
     * @param request For detecting the user's client locale.
     * @param locale If not given, the client's language (browser) will be used.
     * @param keysOnly If true, only the keys will be returned. Default is false.
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @see JsonUtils.toJson
     */
    @GetMapping("list")
    fun getList(
        request: HttpServletRequest,
        @RequestParam("prettyPrinter", required = false) prettyPrinter: Boolean?,
        @RequestParam("keysOnly", required = false) keysOnly: Boolean?,
        @RequestParam("locale", required = false) locale: String?
    ): String {
        val localeObject: Locale?
        if (StringUtils.isNotBlank(locale)) {
            localeObject = Locale(locale)
        } else {
            localeObject = RestUtils.getUserLocale(request)
        }
        val translations: Map<String, String> =
            I18nClientMessages.getInstance().getAllMessages(localeObject, keysOnly == true)
        return JsonUtils.toJson(translations, prettyPrinter)
    }
}
