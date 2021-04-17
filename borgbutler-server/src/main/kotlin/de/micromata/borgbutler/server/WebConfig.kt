package de.micromata.borgbutler.server

import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.ViewResolver
import org.springframework.web.servlet.config.annotation.*
import org.springframework.web.servlet.view.InternalResourceViewResolver
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.Path


private val log = KotlinLogging.logger {}

@Configuration
@EnableWebMvc
open class WebConfig : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        if (RunningMode.webBundled()) {
            registry.addResourceHandler("/**").addResourceLocations(*CLASSPATH_RESOURCE_LOCATIONS);
        } else {
            val localWebDir = Paths.get(".", "borgbutler-webapp", "build").toFile()
            if (localWebDir.isDirectory) {
                registry.addResourceHandler("/**").addResourceLocations(localWebDir.toURI().toString());
            } else {
                log.warn { "*** Can't locate React application. You should run npm/yarn build/start and/or check this path: ${localWebDir.absolutePath}" }
            }
        }
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
