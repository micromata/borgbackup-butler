package de.micromata.borgbutler.server

import de.micromata.borgbutler.EmphasizedLogSupport
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.ViewResolver
import org.springframework.web.servlet.config.annotation.*
import org.springframework.web.servlet.view.InternalResourceViewResolver
import java.nio.file.Paths


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
        // https://stackoverflow.com/questions/39331929/spring-catch-all-route-for-index-html
        registry.addViewController("/")
            .setViewName("forward:/index.html")
        // Map "/word", "/word/word", and "/word/word/word" - except for anything starting with "/api/..." or ending with
        // a file extension like ".js" - to index.html. By doing this, the client receives and routes the url. It also
        // allows client-side URLs to be bookmarked.
        // Single directory level - no need to exclude "rest" (api)
        registry.addViewController("/{x:[\\w\\-]+}")
            .setViewName("forward:/index.html")
        // Multi-level directory path, need to exclude "rest" on the first part of the path
        registry.addViewController("/{x:^(?!rest$).*$}/**/{y:[\\w\\-]+}")
            .setViewName("forward:/index.html")
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        if (RunningMode.webDevelopment) {
            val emphasizedLog = EmphasizedLogSupport(log)
            emphasizedLog.logLevel = EmphasizedLogSupport.LogLevel.WARN
            emphasizedLog.log("ATTENTION!")
            emphasizedLog.log("")
            emphasizedLog.log("Running in web dev mode!")
            emphasizedLog.log("")
            emphasizedLog.log("Don't deliver this app in dev mode due to security reasons (CrossOriginFilter is set)!")
            emphasizedLog.logEnd()
            registry.addMapping("/**")
        }
    }

    companion object {
        private val CLASSPATH_RESOURCE_LOCATIONS = arrayOf("classpath:/webapp/static/", "classpath:/webapp/")
    }
}
