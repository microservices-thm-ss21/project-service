package de.thm.mni.microservices.gruppe6.project.model.persistence

import de.thm.mni.microservices.gruppe6.lib.classes.projectService.Member
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

interface MemberRepository : ReactiveCrudRepository<Member, UUID> {

    @Query("select * from members where project_id = :id")
    fun getMembersByProjectID(id: UUID): Flux<Member>

    @Query("select * from members where project_id = :projectId and user_id = :userId")
    fun findMemberOfProject(projectId: UUID, userId: UUID): Mono<Member>

    @Query("select * from members where user_id = :userId")
    fun findAllByUserId(userId: UUID): Flux<Member>

    fun deleteByUserId(userId: UUID): Mono<Void>

    fun existsByUserIdAndProjectId(userId: UUID, projectId: UUID): Mono<Boolean>
}
