package de.thm.mni.microservices.gruppe6.project.model.persistence

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

interface MemberRepository : ReactiveCrudRepository<Member, UUID> {

    @Query("select * from members where project_id = :id")
    fun getMembersByProjectID(id: UUID): Flux<Member>

    @Query("delete from members where project_id = :id")
    fun deleteAllMembersByProjectID(id: UUID): Mono<Void>

    @Query("delete from members where project_id = :id and user_id in :userIds")
    fun deleteMembersByProjectID(id: UUID, userIds: List<UUID>): Mono<Void>

    @Query("select * from members where project_id = :projectId and user_id = :userId")
    fun findMemberOfProject(projectId: UUID, userId: UUID): Mono<Member>

    @Query("select * from members where user_id = :userId")
    fun findAllByUserId(userId: UUID): Flux<Member>
}
