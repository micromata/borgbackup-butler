package de.micromata.borgbutler.server

import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.ViewResolver
import org.springframework.web.servlet.config.annotation.*
import org.springframework.web.servlet.view.InternalResourceViewResolver


private val log = KotlinLogging.logger {}

@Configuration
@EnableWebMvc
open class WebConfig : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**").addResourceLocations(*CLASSPATH_RESOURCE_LOCATIONS);
    }

    @Bean
    open fun getViewResolver(): ViewResolver? {
        val resolver = InternalResourceViewResolver()
        resolver.setSuffix(".html")
        return resolver
    }

    override fun configurePathMatch(configurer: PathMatchConfigurer) {
        configurer.isUseTrailingSlashMatch = true
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/")
            .setViewName("forward:/index.html")
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        if (RunningMode.webDevelopment) {
            log.warn("************************************")
            log.warn("***********               **********")
            log.warn("*********** ATTENTION!    **********")
            log.warn("***********               **********")
            log.warn("*********** Running in    **********")
            log.warn("*********** web dev mode! **********")
            log.warn("***********               **********")
            log.warn("************************************")
            log.warn("Don't deliver this app in dev mode due to security reasons (CrossOriginFilter is set)!")
            registry.addMapping("/**")
        }
    }

    companion object {
        private val CLASSPATH_RESOURCE_LOCATIONS = arrayOf("classpath:/webapp/static/", "classpath:/webapp/")
    }
}
