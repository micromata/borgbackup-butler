package de.micromata.borgbutler.server.rest

import de.micromata.borgbutler.json.JsonUtils
import de.micromata.borgbutler.server.Languages
import de.micromata.borgbutler.server.Version
import org.apache.commons.lang3.StringUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/rest")
class VersionRest {
    /**
     *
     * @param request For detecting the user's client locale.
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @see JsonUtils.toJson
     */
    @GetMapping("version")
    fun getVersion(
        request: HttpServletRequest,
        @RequestParam("prettyPrinter", required = false) prettyPrinter: Boolean?
    ): String {
        val user = RestUtils.getUser()
        var language = Languages.asString(user.getLocale())
        if (StringUtils.isBlank(language)) {
            val locale: Locale = request.locale
            language = locale.getLanguage()
        }
        val version = MyVersion(language, RestUtils.checkLocalDesktopAvailable(request) == null)
        return JsonUtils.toJson(version, prettyPrinter)
    }

    inner class MyVersion(language: String, localDesktopAvailable: Boolean) {
        private val version: Version
        val language: String
        val isLocalDesktopAvailable: Boolean
        val appName: String
            get() = version.appName

        fun getVersion(): String {
            return version.version
        }

        val buildDateUTC: String
            get() = version.buildDateUTC
        val buildDate: Date
            get() = version.buildDate

        /**
         * @return Version of the available update, if exist. Otherwise null.
         */
        val updateVersion: String?
            get() = version.updateVersion

        init {
            version = Version.getInstance()
            this.language = language
            isLocalDesktopAvailable = localDesktopAvailable
        }
    }
}
