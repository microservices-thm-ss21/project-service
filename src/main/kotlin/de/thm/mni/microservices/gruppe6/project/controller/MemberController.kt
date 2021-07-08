package de.thm.mni.microservices.gruppe6.project.controller

import de.thm.mni.microservices.gruppe6.lib.exception.ServiceException
import de.thm.mni.microservices.gruppe6.project.model.message.MemberDTO
import de.thm.mni.microservices.gruppe6.project.model.persistence.Member
import de.thm.mni.microservices.gruppe6.project.service.MemberDbService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@RestController
@CrossOrigin
@RequestMapping("/api/projects/{id}/members")
class MemberController(@Autowired val memberService: MemberDbService) {

    /**
     * Creates new members for a project with given id
     */
    @PostMapping("")
    @ResponseStatus(value = HttpStatus.CREATED)
    fun createMembers(@PathVariable id: UUID, @RequestBody members: List<MemberDTO>): Flux<Member> =
        memberService.createMembers(id, members).onErrorResume {
            Mono.error(
                ServiceException(
                    HttpStatus.CONFLICT,
                    "Project or Member(s) does not exist",
                    it
                )
            )
        }

    /**
     * Get all members of a given project
     * @param id: project id
     */
    @GetMapping("")
    fun getMembers(@PathVariable id: UUID): Flux<Member> = memberService.getMembers(id)

    /**
     * Check if user of given id is member of a project with given id
     * @param id: project id
     * @param userId: user id
     */
    @GetMapping("{userId}/exists")
    fun isMember(@PathVariable id: UUID, @PathVariable userId: UUID): Mono<Boolean> = memberService.isMember(id, userId)

    /**
     * Deletes all given members of given project
     * @param id: project id
     */
    @DeleteMapping("")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    fun deleteMembers(@PathVariable id: UUID): Mono<Void> = memberService.deleteAllMembers(id)

    /**
     * Update the roles of members within a given project
     * @param id: project id
     */
    @PutMapping("")
    fun updateMembers(@PathVariable id: UUID, @RequestBody members: List<MemberDTO>): Flux<Member> =
        memberService.updateMemberRoles(id, members).onErrorResume {
            Mono.error(
                ServiceException(
                    HttpStatus.CONFLICT,
                    "Project or Member(s) does not exist",
                    it
                )
            )
        }
}
