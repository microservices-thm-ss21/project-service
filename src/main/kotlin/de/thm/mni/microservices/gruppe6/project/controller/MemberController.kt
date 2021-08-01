package de.thm.mni.microservices.gruppe6.project.controller

import de.thm.mni.microservices.gruppe6.lib.classes.projectService.Member
import de.thm.mni.microservices.gruppe6.lib.classes.projectService.ProjectRole
import de.thm.mni.microservices.gruppe6.lib.classes.userService.User
import de.thm.mni.microservices.gruppe6.lib.exception.ServiceException
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

    // toDo: remove when jwt works
    val jwtUser = User(
        UUID.fromString("a443ffd0-f7a8-44f6-8ad3-87acd1e91042"),
        "Peter_Zwegat",
        "password",
        "Peter",
        "Zwegat",
        "peter.zwegat@mni.thm.de",
        LocalDate.now(),
        LocalDateTime.now(),
        "USER",
        null
    )

    /**
     * Creates new members for a project with given id
     */
    @PostMapping("user/{userId}/role/{userRole}")
    @ResponseStatus(value = HttpStatus.CREATED)
    fun createMember(
        @PathVariable projectId: UUID,
        @PathVariable userId: UUID,
        @PathVariable userRole: ProjectRole
    ): Mono<Member> =  memberService.createMember(
            projectId,
            jwtUser,
            userId,
            userRole
        )

    /**
     * Get all members of a given project
     * @param projectId: project id
     */
    @GetMapping("")
    fun getMembers(@PathVariable projectId: UUID): Flux<Member> = memberService.getMembers(projectId)


    /**
     * Check if user of given id is member of a project with given id
     * @param projectId: project id
     * @param userId: user id
     */
    @GetMapping("{userId}/exists")
    fun isMember(@PathVariable projectId: UUID, @PathVariable userId: UUID): Mono<Boolean> {
        logger.debug("isMember $projectId $userId")
        return memberService.isMember(projectId, userId)
    }

    /**
     * Deletes all members of given project
     * @param projectId: project id
     */
    @DeleteMapping("user/{userId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    fun deleteMember(@PathVariable projectId: UUID, @PathVariable userId: UUID): Mono<Void> =
        memberService.deleteMember(projectId, jwtUser, userId)


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
        @PathVariable role: ProjectRole
    ): Mono<Member> =
        memberService.updateMemberRole(projectId, jwtUser, userId, role).onErrorResume {
            Mono.error(
                ServiceException(
                    HttpStatus.CONFLICT,
                    "Project or Member(s) does not exist",
                    it
                )
            )
    }

}
