package de.micromata.borgbutler.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.io.JsonStringEncoder
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.StringWriter

private val log = KotlinLogging.logger {}

object JsonUtils {
    /**
     * @param obj
     * @param prettyPrinter If true, the json output will be pretty printed (human readable with new lines and indenting).
     * @return
     */
    @JvmOverloads
    @JvmStatic
    fun toJson(obj: Any?, prettyPrinter: Boolean? = false): String {
        if (obj == null) {
            return ""
        }
        val objectMapper = ObjectMapper()
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        return try {
            if (prettyPrinter == true) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj)
            } else {
                val writer = StringWriter()
                objectMapper.writeValue(writer, obj)
                writer.toString()
            }
        } catch (ex: IOException) {
            log.error(ex.message, ex)
            ""
        }
    }

    @JvmStatic
    fun toJson(str: String?): String {
        return if (str == null) "" else String(JsonStringEncoder.getInstance().quoteAsString(str))
    }

    @JvmStatic
    fun <T> fromJson(clazz: Class<T>?, json: String?): T? {
        val objectMapper = ObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return try {
            objectMapper.readValue(json, clazz)
        } catch (ex: IOException) {
            log.error(ex.message, ex)
            null
        }
    }

    @JvmStatic
    fun <T> fromJson(type: TypeReference<T>?, json: String): T? {
        try {
            return ObjectMapper().readValue(json, type)
        } catch (ex: Exception) {
            log.error("Json: '" + json + "': " + ex.message, ex)
        }
        return null
    }
}
