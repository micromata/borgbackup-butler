package de.micromata.borgbutler.server.rest

import de.micromata.borgbutler.cache.ButlerCache
import de.micromata.borgbutler.data.Repository
import de.micromata.borgbutler.json.JsonUtils
import mu.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/rest/repos")
class ReposRest {
    /**
     * @return A list of repositories of type [BorgRepository].
     * @see JsonUtils.toJson
     */
    @GetMapping("list")
    fun getList(): List<Repository> {
        return ButlerCache.getInstance().allRepositories
    }

    /**
     *
     * @param id id or name of repo.
     * @return [Repository] (without list of archives) as json string.
     * @see JsonUtils.toJson
     */
    @GetMapping("repo")
    fun getRepo(@RequestParam("id") id: String): Repository? {
        return ButlerCache.getInstance().getRepository(id)
    }

    /**
     *
     * @param id id or name of repo.
     * @return [Repository] (including list of archives) as json string.
     * @see JsonUtils.toJson
     */
    @GetMapping("repoArchiveList")
    fun getRepoArchiveList(
        @RequestParam("id") id: String,
        @RequestParam("force", required = false) force: Boolean?
    ): Repository? {
        if (force == true) {
            val repo: Repository = ButlerCache.getInstance().getRepository(id)
            ButlerCache.getInstance().clearRepoCacheAccess(repo)
        }
        return ButlerCache.getInstance().getRepositoryArchives(id)
    }
}
