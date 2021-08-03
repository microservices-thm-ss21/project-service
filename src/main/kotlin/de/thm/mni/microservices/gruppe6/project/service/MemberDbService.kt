package de.thm.mni.microservices.gruppe6.project.service

import de.thm.mni.microservices.gruppe6.lib.classes.projectService.Member
import de.thm.mni.microservices.gruppe6.lib.classes.projectService.ProjectRole
import de.thm.mni.microservices.gruppe6.lib.classes.userService.GlobalRole
import de.thm.mni.microservices.gruppe6.lib.classes.userService.User
import de.thm.mni.microservices.gruppe6.lib.event.DomainEventChangedStringUUID
import de.thm.mni.microservices.gruppe6.lib.event.DomainEventChangedUUID
import de.thm.mni.microservices.gruppe6.lib.event.DomainEventCode
import de.thm.mni.microservices.gruppe6.lib.event.EventTopic
import de.thm.mni.microservices.gruppe6.lib.exception.ServiceException
import de.thm.mni.microservices.gruppe6.project.model.persistence.MemberRepository
import de.thm.mni.microservices.gruppe6.project.model.persistence.ProjectRepository
import de.thm.mni.microservices.gruppe6.project.model.persistence.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.switchIfEmpty
import java.util.*

@Component
class MemberDbService(
    @Autowired private val userRepo: UserRepository,
    @Autowired private val projectRepo: ProjectRepository,
    @Autowired private val memberRepo: MemberRepository,
    @Autowired private val sender: JmsTemplate
) {


    private val logger = LoggerFactory.getLogger(this::class.java)


    /**
     * Gets all Members of a given Project id
     * @param projectId: project id
     */
    fun getMembers(projectId: UUID): Flux<Member> {
        logger.debug("getMembers $projectId")
        return memberRepo.getMembersByProjectID(projectId)
    }

    /**
     * Returns true if user is member in project
     * @param projectId
     * @param userId
     * @return isMember
     */
    fun isMember(projectId: UUID, userId: UUID): Mono<Boolean> {
        logger.debug("isMember $projectId $userId")
        return memberRepo.existsByUserIdAndProjectId(userId, projectId)
    }

    /**
     * Gets all Project Ids in which the User Id is included as a Member
     * @param userId: user id
     */
    fun getAllProjectIdsOfMember(userId: UUID): Flux<UUID> {
        logger.debug("getAllProjectIdsOfMember $userId")
        return memberRepo.findAllByUserId(userId).map {
            it.projectId
        }
    }

    /**
     * Stores all given members
     * @param projectId: project id
     */
    fun createMember(projectId: UUID, requester: User, userId: UUID, role: ProjectRole): Mono<Member> {
        logger.debug("createMember $projectId $requester $userId $role")
        return checkHardPermissions(projectId, requester)
            .flatMap { userRepo.existsById(userId) }
            .filter { it }
            .switchIfEmpty(Mono.error(ServiceException(HttpStatus.NOT_FOUND, "User not existing")))
            .flatMap {
                memberRepo.save(Member(null, projectId, userId, role.name))
            }.publishOn(Schedulers.boundedElastic())
            .map {
                sender.convertAndSend(
                    EventTopic.DomainEvents_ProjectService.topic,
                    DomainEventChangedStringUUID(
                        DomainEventCode.PROJECT_CHANGED_MEMBER,
                        projectId,
                        userId,
                        null,
                        it.projectRole
                    )
                )
                it
            }
    }

    /**
     * Delete a member from a project
     * @param projectId
     * @param requester
     * @param userId
     */
    fun deleteMember(projectId: UUID, requester: User, userId: UUID): Mono<Void> {
        logger.debug("deleteMember $projectId $requester $userId")
        return checkHardPermissions(projectId, requester)
            .flatMap {
                memberRepo.deleteById(userId)
            }
            .publishOn(Schedulers.boundedElastic())
            .map {
                sender.convertAndSend(
                    EventTopic.DomainEvents_ProjectService.topic,
                    DomainEventChangedUUID(
                        DomainEventCode.PROJECT_CHANGED_MEMBER,
                        projectId,
                        userId,
                        null
                    )
                )
                it
            }
    }

    /**
     * Update the roles of members within a given project
     * @param projectId: project id
     * @param requester
     * @param userId
     * @param role
     */
    fun updateMemberRole(projectId: UUID, requester: User, userId: UUID, role: ProjectRole): Mono<Member> {
        logger.debug("updateMemberRole $projectId $requester $userId $role")
        return checkHardPermissions(projectId, requester)
            .flatMap { memberRepo.findMemberOfProject(projectId, userId) }
            .flatMap { oldMember ->
                memberRepo.save(Member(oldMember.id, oldMember.projectId, oldMember.userId, role.name))
                    .map { newMember ->
                        Pair(oldMember, newMember)
                    }
            }
            .publishOn(Schedulers.boundedElastic())
            .map {
                sender.convertAndSend(
                    EventTopic.DomainEvents_ProjectService.topic,
                    DomainEventChangedStringUUID(
                        DomainEventCode.PROJECT_CHANGED_MEMBER,
                        projectId,
                        userId,
                        it.first.projectRole,
                        it.second.projectRole
                    )
                )
                it.second
            }
    }

    /**
     * Check if user is member of project or creator
     * @param projectId
     * @param user
     */
    fun checkSoftPermissions(projectId: UUID, user: User): Mono<UUID> {
        logger.debug("checkSoftPermissions $projectId $user")
        return Mono.zip(projectRepo.findById(projectId), isMember(projectId, user.id!!))
            .filter {
                it.t1.creatorId == user.id!! || it.t2 || user.globalRole != GlobalRole.USER.name
            }.switchIfEmpty {
                Mono.error(ServiceException(HttpStatus.FORBIDDEN, "No permissions"))
            }.map { projectId }
    }

    /**
     * check if user is admin global or project or creator
     * @param projectId
     * @param user
     */
    fun checkHardPermissions(projectId: UUID, user: User): Mono<UUID> {
        logger.debug("checkHardPermissions $projectId $user")

        return isMember(projectId, user.id!!)
                .flatMap { userIsMember ->
                    if (userIsMember) {
                        Mono.zip(projectRepo.findById(projectId), memberRepo.findMemberOfProject(projectId, user.id!!))
                                .filter {
                                    it.t1.creatorId == user.id!! || it.t2.projectRole == ProjectRole.ADMIN.name || user.globalRole == GlobalRole.ADMIN.name
                                }.map { projectId }
                    } else {
                        Mono.just(projectId).filter {
                            user.globalRole == GlobalRole.ADMIN.name
                        }
                    }
                }.switchIfEmpty {
                    Mono.error(ServiceException(HttpStatus.FORBIDDEN, "No permissions"))
                }
    }
}
