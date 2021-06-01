package de.thm.mni.microservices.gruppe6.project.service

import de.thm.mni.microservices.gruppe6.lib.event.ProjectDataEvent
import de.thm.mni.microservices.gruppe6.project.model.message.MemberDTO
import de.thm.mni.microservices.gruppe6.project.model.persistence.Member
import de.thm.mni.microservices.gruppe6.project.model.persistence.MemberRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Component
class MemberDbService(@Autowired private val memberRepo: MemberRepository) {

    /**
     * Gets all Members of a given Project id
     * @param id: project id
     */
    fun getMembers(id: UUID): Flux<Member> = memberRepo.getMembersByProjectID(id)

    /**
     * Stores all given members
     * @param projectId: project id
     */
    fun createMembers(projectId: UUID, members: List<MemberDTO>?): Flux<Member> {
        if (members != null) {
            return Flux.fromIterable(members).flatMap { memberDTO -> memberRepo.save(Member(projectId, memberDTO)) }
        }
        return Flux.empty()
    }

    /**
     * Delete all members of a project given its id
     * @param id: project id
     */
    fun deleteAllMembers(id: UUID): Mono<Void> {
        return memberRepo.deleteAllMembersByProjectID(id)
    }

    /**
     * Delete given members of a project given its id
     * @param id: project id
     */
    fun deleteMembers(id: UUID, members: List<Member>): Mono<Void> {
        return memberRepo.deleteMembersByProjectID(id, members.map { m -> m.userId })
    }

    /**
     * Update the roles of members within a given project
     * @param id: project id
     */
    fun updateMemberRoles(id: UUID, members: List<MemberDTO>): Flux<Member> {
        return Flux.fromIterable(members).flatMap { memberRepo.findMemberOfProject(id, it.userId!!) }
            .zipWithIterable(members)
            .flatMap { memberRepo.save(Member(it.t1.id, it.t1.projectId, it.t1.userId, it.t2.projectRole!!)) }
    }
}
