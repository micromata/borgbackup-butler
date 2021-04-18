package de.micromata.borgbutler.server.rest

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.micromata.borgbutler.server.RunningMode
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

private val log = KotlinLogging.logger {}

@Configuration
open class JacksonConfig {
    private var objectMapper: ObjectMapper? = null

    @Bean
    @Primary
    open fun objectMapper(): ObjectMapper {
        objectMapper?.let {
            return it
        }
        val mapper = ObjectMapper()
        mapper.registerModule(KotlinModule())
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        val failOnUnknownJsonProperties = RunningMode.runningInIDE
        if (failOnUnknownJsonProperties) {
            log.warn("Unknown JSON properties are not allowed in REST call, due to configuration in projectforge.properties:projectforge.rest.json.failOnUnknownJsonProperties (OK, but Rest calls may fail).")
        }
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnknownJsonProperties) // Should be true in development mode!
        objectMapper = mapper
        return mapper
    }
}
