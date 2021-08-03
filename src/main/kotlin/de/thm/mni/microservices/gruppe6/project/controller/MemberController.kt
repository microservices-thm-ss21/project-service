package de.thm.mni.microservices.gruppe6.project.controller

import de.thm.mni.microservices.gruppe6.lib.classes.authentication.ServiceAuthentication
import de.thm.mni.microservices.gruppe6.lib.classes.projectService.Member
import de.thm.mni.microservices.gruppe6.lib.classes.projectService.ProjectRole
import de.thm.mni.microservices.gruppe6.lib.classes.userService.User
import de.thm.mni.microservices.gruppe6.lib.exception.ServiceException
import de.thm.mni.microservices.gruppe6.lib.exception.coverUnexpectedException
import de.thm.mni.microservices.gruppe6.project.service.MemberDbService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@RestController
@CrossOrigin
@RequestMapping("/api/projects/{projectId}/members")
class MemberController(@Autowired val memberService: MemberDbService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Creates new members for a project with given id
     */
    @PostMapping("user/{userId}/role/{userRole}")
    @ResponseStatus(value = HttpStatus.CREATED)
    fun addMember(
        @PathVariable projectId: UUID,
        @PathVariable userId: UUID,
        @PathVariable userRole: ProjectRole,
        auth: ServiceAuthentication
    ): Mono<Member> =  memberService.addMember(
            projectId,
            auth.user!!,
            userId,
            userRole
        ).onErrorResume { Mono.error(coverUnexpectedException(it)) }

    /**
     * Get all members of a given project
     * @param projectId: project id
     */
    @GetMapping("")
    fun getMembers(@PathVariable projectId: UUID): Flux<Member> = memberService.getMembers(projectId).onErrorResume { Mono.error(coverUnexpectedException(it)) }


    /**
     * Check if user of given id is member of a project with given id
     * @param projectId: project id
     * @param userId: user id
     */
    @GetMapping("{userId}/exists")
    fun isMember(@PathVariable projectId: UUID, @PathVariable userId: UUID): Mono<Boolean> {
        logger.debug("isMember $projectId $userId")
        return memberService.isMember(projectId, userId).onErrorResume { Mono.error(coverUnexpectedException(it)) }
    }

    /**
     * Deletes all members of given project
     * @param projectId: project id
     */
    @DeleteMapping("user/{userId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    fun deleteMember(@PathVariable projectId: UUID, @PathVariable userId: UUID, auth: ServiceAuthentication): Mono<Void> =
        memberService.deleteMember(projectId, auth.user!!, userId).onErrorResume { Mono.error(coverUnexpectedException(it)) }.flatMap { Mono.empty() }


    /**
     * Update the role of member within a given project
     * @param projectId: project id
     * @param userId
     * @param role
     */
    @PutMapping("user/{userId}/role/{role}")
    fun updateMemberRole(
        @PathVariable projectId: UUID,
        @PathVariable userId: UUID,
        @PathVariable role: ProjectRole,
        auth: ServiceAuthentication
    ): Mono<Member> =
        memberService.updateMemberRole(projectId, auth.user!!, userId, role)
                .onErrorResume { Mono.error(coverUnexpectedException(it)) }

}
