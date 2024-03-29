package de.micromata.borgbutler.server.rest

import de.micromata.borgbutler.BorgQueueExecutor
import de.micromata.borgbutler.json.JsonUtils
import de.micromata.borgbutler.server.BorgInstallation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rest/system")
class SystemInfoRest {
    /**
     * @return The total number of jobs queued or running (and other statistics): [de.micromata.borgbutler.BorgQueueStatistics].
     * @see JsonUtils.toJson
     */
    @GetMapping("info")
    fun statistics(): SystemInfo {
        val borgConfig = BorgInstallation.getInstance().borgConfig
        val systemInfo = SystemInfo()
            .setQueueStatistics(BorgQueueExecutor.getInstance().statistics)
            .setConfigurationOK(borgConfig.isVersionOK)
            .setBorgConfig(borgConfig)
        return systemInfo
    }
}
