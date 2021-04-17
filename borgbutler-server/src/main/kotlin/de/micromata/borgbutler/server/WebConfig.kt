package de.micromata.borgbutler.server

import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.ViewResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.view.InternalResourceViewResolver


private val log = KotlinLogging.logger {}

@Configuration
@EnableWebMvc
open class WebConfig : WebMvcConfigurer {
    @Bean
    open fun internalResourceViewResolver(): ViewResolver {
        val bean = InternalResourceViewResolver()
        if (RunningMode.webBundled()) {
            bean.setPrefix("/web/")
        } else {
            bean.setPrefix("borgbutler-webapp/build/")
        }
        bean.setSuffix(".html")
        return bean
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        if (RunningMode.webDevelopment) {
            log.warn("*********************************")
            log.warn("***********            **********")
            log.warn("*********** ATTENTION! **********")
            log.warn("***********            **********")
            log.warn("*********** Running in **********")
            log.warn("*********** dev mode!  **********")
            log.warn("***********            **********")
            log.warn("*********************************")
            log.warn("Don't deliver this app in dev mode due to security reasons (CrossOriginFilter is set)!")
            registry.addMapping("/**")
        }
    }
}
