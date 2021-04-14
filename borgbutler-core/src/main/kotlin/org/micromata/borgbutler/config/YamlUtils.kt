package org.micromata.borgbutler.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLParser
import mu.KotlinLogging
import java.io.IOException
import java.io.StringWriter

private val log = KotlinLogging.logger {}

object YamlUtils {
    /**
     * @param obj
     * @return
     */
    fun toYaml(obj: Any?): String {
        if (obj == null) {
            return ""
        }
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        return try {
            val writer = StringWriter()
            objectMapper.writeValue(writer, obj)
            writer.toString()
        } catch (ex: IOException) {
            log.error(ex.message, ex)
            ""
        }
    }

    fun <T> fromYaml(clazz: Class<T>?, json: String?): T? {
        return try {
            objectMapper.readValue(json, clazz)
        } catch (ex: IOException) {
            log.error(ex.message, ex)
            null
        }
    }

    fun <T> fromYaml(type: TypeReference<T>?, json: String): T? {
        try {
            return objectMapper.readValue(json, type)
        } catch (ex: Exception) {
            log.error("Json: '" + json + "': " + ex.message, ex)
        }
        return null
    }

    private val objectMapper: ObjectMapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))

    init {
        objectMapper.findAndRegisterModules()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }
}
