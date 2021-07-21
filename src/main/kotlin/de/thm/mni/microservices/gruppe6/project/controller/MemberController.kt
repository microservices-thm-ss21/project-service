package de.thm.mni.microservices.gruppe6.project.controller

import de.thm.mni.microservices.gruppe6.lib.exception.ServiceException
import de.thm.mni.microservices.gruppe6.project.model.message.MemberDTO
import de.thm.mni.microservices.gruppe6.project.model.persistence.Member
import de.thm.mni.microservices.gruppe6.project.service.MemberDbService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@RestController
@CrossOrigin
@RequestMapping("/api/projects/{projectId}/members")
class MemberController(@Autowired val memberService: MemberDbService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Creates new members for a project with given id
     */
    @PostMapping("user/{userId}")
    @ResponseStatus(value = HttpStatus.CREATED)
    fun createMembers(@PathVariable projectId: UUID, @PathVariable userId: UUID, @RequestBody members: List<MemberDTO>): Flux<Member> {
        logger.debug("User $userId creates members inside project $projectId ")
        return memberService.createMembers(projectId, userId, members)
    }


    /**
     * Get all members of a given project
     * @param id: project id
     */
    @GetMapping("")
    fun getMembers(@PathVariable projectId: UUID): Flux<Member> = memberService.getMembers(projectId)

    /**
     * Check if user of given id is member of a project with given id
     * @param projectId: project id
     * @param userId: user id
     */
    @GetMapping("{userId}/exists")
    fun isMember(@PathVariable projectId: UUID, @PathVariable userId: UUID): Mono<Boolean> = memberService.isMember(projectId, userId)

    /**
     * Deletes all given members of given project
     * @param projectId: project id
     */
    @DeleteMapping("user/{userId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    fun deleteMembers(@PathVariable projectId: UUID, @PathVariable userId: UUID): Mono<Void> {
        logger.debug("User $userId wants to delete all members inside project $projectId")
        return memberService.deleteAllMembers(projectId, userId)
    }

    /**
     * Update the roles of members within a given project
     * @param projectId: project id
     */
    @PutMapping("user/{userId}")
    fun updateMembers(@PathVariable projectId: UUID, @PathVariable userId: UUID, @RequestBody members: List<MemberDTO>): Flux<Member> {
        logger.debug("User $userId wants to update members inside project $projectId")
        return memberService.updateMemberRoles(projectId, userId, members).onErrorResume {
            Mono.error(
                    ServiceException(
                            HttpStatus.CONFLICT,
                            "Project or Member(s) does not exist",
                            it
                    )
            )
        }
    }

}
