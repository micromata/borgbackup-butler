package de.micromata.borgbutler.server

import mu.KotlinLogging
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

private val log = KotlinLogging.logger {}

@Controller
open class WebConntroller {
    @RequestMapping("/")
    fun index(): String {
        return "index"
    }
}
