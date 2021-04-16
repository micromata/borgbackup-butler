package de.micromata.borgbutler.server.rest

import de.micromata.borgbutler.cache.ButlerCache
import de.micromata.borgbutler.data.Repository
import de.micromata.borgbutler.json.JsonUtils
import mu.KotlinLogging
import org.apache.commons.collections4.CollectionUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/rest/repos")
class ReposRest {
    /**
     *
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @return A list of repositories of type [BorgRepository].
     * @see JsonUtils.toJson
     */
    @GetMapping("list")
    fun getList(@RequestParam("prettyPrinter", required = false) prettyPrinter: Boolean?): String {
        val repositories: List<Repository?> = ButlerCache.getInstance().getAllRepositories()
        return if (CollectionUtils.isEmpty(repositories)) {
            "[]"
        } else JsonUtils.toJson(repositories, prettyPrinter)
    }

    /**
     *
     * @param id id or name of repo.
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @return [Repository] (without list of archives) as json string.
     * @see JsonUtils.toJson
     */
    @GetMapping("repo")
    fun getRepo(@RequestParam("id") id: String,
                @RequestParam("prettyPrinter", required = false) prettyPrinter: Boolean?): String {
        val repository: Repository = ButlerCache.getInstance().getRepository(id)
        return JsonUtils.toJson(repository, prettyPrinter)
    }

    /**
     *
     * @param id id or name of repo.
     * @param prettyPrinter If true then the json output will be in pretty format.
     * @return [Repository] (including list of archives) as json string.
     * @see JsonUtils.toJson
     */
    @GetMapping("repoArchiveList")
    fun getRepoArchiveList(
        @RequestParam("id") id: String,
        @RequestParam("force", required = false) force: Boolean?,
        @RequestParam("prettyPrinter", required = false) prettyPrinter: Boolean?
    ): String {
        if (force == true) {
            val repo: Repository = ButlerCache.getInstance().getRepository(id)
            ButlerCache.getInstance().clearRepoCacheAccess(repo)
        }
        val repository: Repository = ButlerCache.getInstance().getRepositoryArchives(id)
        return JsonUtils.toJson(repository, prettyPrinter)
    }
}
