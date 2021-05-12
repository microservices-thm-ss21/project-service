package de.thm.mni.microservices.gruppe6.project.controller

import de.thm.mni.microservices.gruppe6.project.model.message.MemberDTO
import de.thm.mni.microservices.gruppe6.project.model.message.ProjectDTO
import de.thm.mni.microservices.gruppe6.project.model.persistence.Member
import de.thm.mni.microservices.gruppe6.project.model.persistence.Project
import de.thm.mni.microservices.gruppe6.project.service.MemberDbService
import de.thm.mni.microservices.gruppe6.project.service.ProjectDbService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/api/projects/{id}/members")
class MemberController(@Autowired val memberService: MemberDbService) {

    /**
     * Creates new members for a project with given id
     */
    @PostMapping("")
    fun createMembers(@PathVariable id: UUID, @RequestBody members: List<MemberDTO>): Flux<Member> = memberService.createMembers(id, members)

    /**
     * Get all members of a given project
     * @param id: project id
     */
    @GetMapping("")
    fun getMembers(@PathVariable id: UUID): Flux<Member> = memberService.getMembers(id)

    /**
     * Deletes all given members of given project
     * @param id: project id
     */
    @DeleteMapping("")
    fun deleteMembers(@PathVariable id: UUID): Mono<Void> = memberService.deleteAllMembers(id)

    /**
     * Update the roles of members within a given project
     * @param id: project id
     */
    @PutMapping("")
    fun updateMembers(@PathVariable id: UUID, @RequestBody members: List<MemberDTO>): Flux<Member> = memberService.updateMemberRoles(id, members)
}
