package de.thm.mni.microservices.gruppe6.project.service

import de.thm.mni.microservices.gruppe6.lib.classes.projectService.ProjectRole
import de.thm.mni.microservices.gruppe6.lib.event.*
import de.thm.mni.microservices.gruppe6.lib.exception.ServiceException
import de.thm.mni.microservices.gruppe6.project.model.message.MemberDTO
import de.thm.mni.microservices.gruppe6.project.model.persistence.Member
import de.thm.mni.microservices.gruppe6.project.model.persistence.MemberRepository
import de.thm.mni.microservices.gruppe6.project.model.persistence.ProjectRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux
import java.util.*

@Component
class MemberDbService(
    @Autowired private val projectRepo: ProjectRepository,
    @Autowired private val memberRepo: MemberRepository,
    @Autowired private val sender: JmsTemplate
) {

    /**
     * Gets all Members of a given Project id
     * @param id: project id
     */
    fun getMembers(projectId: UUID): Flux<Member> = memberRepo.getMembersByProjectID(projectId)

    /**
     * Returns true if user is member in project
     * @param projectId
     * @param userId
     * @return isMember
     */
    fun isMember(projectId: UUID, userId: UUID): Mono<Boolean> = memberRepo.existsByUserIdAndProjectId(userId, projectId)

    /**
     * Returns true if user is member in project
     * @param projectId
     * @param userId
     * @return isMember
     */
    fun isProjectCreator(projectId: UUID, userId: UUID): Mono<Boolean> = projectRepo.existsByCreatorIdAndProjectId(userId, projectId)

    /**
     * Checks if a user has the role "admin" in the given project
     * @param projectId
     * @param userId
     */
    fun isAdmin(projectId: UUID, userId: UUID): Mono<Boolean> = memberRepo.findMemberOfProject(projectId, userId)
        .map { it.projectRole.toLowerCase() == "admin" }

    /**
     * Gets all Project Ids in which the User Id is included as a Member
     * @param userId: user id
     */
    fun getAllProjectIdsOfMember(userId: UUID): Flux<UUID> = memberRepo.findAllByUserId(userId).map {
        it.projectId
    }

    /**
     * Stores all given members
     * @param projectId: project id
     */
    fun createMember(projectId: UUID, requesterId: UUID, userId: UUID, role: ProjectRole): Mono<Member> {
        return projectRepo.findById(projectId).zipWith(isMember(projectId, userId))
            .filter {
                it.t2 || it.t1.creatorId == requesterId
            }.switchIfEmpty {
                Mono.error(ServiceException(HttpStatus.FORBIDDEN, "No permissions to add member to project"))
            }.map {
                it.t1
            }.map {
                memberRepo.save(Member(null, projectId, userId, role.name))
            }.publishOn(Schedulers.boundedElastic())
            .flatMap {
                    Flux.fromIterable(members!!)
                            .flatMap { memberDTO -> memberRepo.save(Member(projectId, memberDTO)) }
                            .publishOn(Schedulers.boundedElastic()).map {
                                sender.convertAndSend(
                                        EventTopic.DomainEvents_ProjectService.topic,
                                        DomainEventChangedStringUUID(
                                                DomainEventCode.PROJECT_CHANGED_MEMBER,
                                                projectId,
                                                it.id,
                                                null,
                                                it.projectRole
                                        )
                                )
                                it
                            }
                }
    }

    /**
     * Delete all members of a project given its id
     * @param id: project id
     */
    fun deleteAllMembers(projectId: UUID, userId: UUID): Mono<Void> {
        return memberRepo.deleteAllMembersByProjectID(projectId)
            .publishOn(Schedulers.boundedElastic()).map {
                sender.convertAndSend(
                    EventTopic.DomainEvents_ProjectService.topic,
                    DomainEventChangedUUID(
                        DomainEventCode.PROJECT_CHANGED_ALL_MEMBERS,
                        projectId,
                        null,
                        null,
                    )
                )
                it
            }
    }

    /**
     * Delete given members of a project given its id
     * @param id: project id
     */
    fun deleteMembers(projectId: UUID, members: List<Member>): Mono<Void> {
        return Flux.fromIterable(members).publishOn(Schedulers.boundedElastic()).map {
            sender.convertAndSend(
                EventTopic.DomainEvents_ProjectService.topic,
                DomainEventChangedUUID(
                    DomainEventCode.PROJECT_CHANGED_MEMBER,
                    projectId,
                    it.userId,
                    null
                )
            )
            it.userId
        }.collectList().flatMap { memberRepo.deleteMembersByProjectID(projectId, members.map { m -> m.userId }) }
    }

    /**
     * Update the roles of members within a given project
     * @param id: project id
     */
    fun updateMemberRoles(projectId: UUID, userId: UUID, members: List<MemberDTO>): Flux<Member> {
        return Flux.fromIterable(members).flatMap { memberRepo.findMemberOfProject(projectId, it.userId!!) }
            .zipWithIterable(members)
            .flatMap { memberRepo.save(Member(it.t1.id, it.t1.projectId, it.t1.userId, it.t2.projectRole!!)) }
            .publishOn(Schedulers.boundedElastic()).map {
                sender.convertAndSend(
                    EventTopic.DomainEvents_ProjectService.topic,
                    DomainEventChangedStringUUID(
                        DomainEventCode.PROJECT_CHANGED_MEMBER,
                        projectId,
                        it.userId,
                        null, // Not sure how to get this information.
                        it.projectRole
                    )
                )
                it
            }
    }
}
