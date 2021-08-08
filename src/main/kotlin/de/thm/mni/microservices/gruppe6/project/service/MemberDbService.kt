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

/**
 * Implements the functionality to handle members.
 */
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
     * @param projectId
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
     * @param userId
     */
    fun getAllProjectIdsOfMember(userId: UUID): Flux<UUID> {
        logger.debug("getAllProjectIdsOfMember $userId")
        return memberRepo.findAllByUserId(userId).map {
            it.projectId
        }
    }

    /**
     * Add a member to a project. Idempotent!
     * @param projectId
     * @param requester
     * @param userId of user that should be added
     * @param role projectRole of the new member
     */
    fun addMember(projectId: UUID, requester: User, userId: UUID, role: ProjectRole): Mono<Member> {
        logger.debug("addMember $projectId $requester $userId $role")
        return isMember(projectId, userId)
            .flatMap { userIsMember ->
                if (userIsMember) {
                    memberRepo.findMemberOfProject(projectId, userId).filter {
                        role.name == it.projectRole
                    }.switchIfEmpty {
                        Mono.error(
                            ServiceException(
                                HttpStatus.CONFLICT,
                                "User is already member of project with a different project role"
                            )
                        )
                    }
                } else {
                    addNewMember(projectId, requester, userId, role)
                        .publishEventChangedMemberRole(projectId,null, role.name)
                }
            }
    }

    /**
     * Create a new member and return it. Checks if the requester has the permission to do so.
     * Does not send any notifications
     * @param projectId
     * @param requester
     * @param userId of user that should be added
     * @param role projectRole of the new member
     */
    fun addNewMember(projectId: UUID, requester: User, userId: UUID, role: ProjectRole): Mono<Member> {
        return checkHardPermissions(projectId, requester)
            .flatMap { userRepo.existsById(userId) }
            .filter { it }
            .switchIfEmpty(Mono.error(ServiceException(HttpStatus.NOT_FOUND, "User not existing")))
            .flatMap {
                memberRepo.save(Member(null, projectId, userId, role.name))
            }
            .publishOn(Schedulers.boundedElastic())
    }

    /**
     * Sends the notifications of the creation of a new member
     * @param projectId
     * @param oldRole Old Role in the Project or null
     * @param newRole New role in the Project or null
     */
    fun Mono<Member>.publishEventChangedMemberRole(projectId: UUID, oldRole: String?, newRole: String?): Mono<Member>{
        return this
            .flatMap { publishEventChangedMemberRole(projectId, it, oldRole, newRole) }
    }

    /**
     * Sends the notifications of the creation of a new member
     * @param projectId
     * @param member New Member Object
     * @param oldRole Old role in the project or null
     * @param newRole new role in the project or null
     */
    fun publishEventChangedMemberRole(projectId: UUID, member: Member, oldRole: String?, newRole: String?): Mono<Member> {
        return Mono.just(member)
                .map {
                    sender.convertAndSend(
                            EventTopic.DomainEvents_ProjectService.topic,
                            DomainEventChangedStringUUID(
                                    DomainEventCode.PROJECT_CHANGED_MEMBER,
                                    projectId,
                                    member.userId,
                                    oldRole,
                                    newRole
                            )
                    )
                    it
                }
    }

    /**
     * Delete a member from a project. Checks if the requester has the permission to do so.
     * @param projectId
     * @param requester
     * @param userId of user that should be deleted
     * @throws ServiceException when permissions are not fulfilled or the member does not exist
     * @return userId of the deleted member
     */
    fun deleteMember(projectId: UUID, requester: User, userId: UUID): Mono<UUID> {
        logger.debug("deleteMember $projectId $requester $userId")
        return checkHardPermissions(projectId, requester)
            .flatMap {
                isMember(projectId, userId).filter {
                    it
                }.switchIfEmpty {
                    Mono.error(ServiceException(HttpStatus.NOT_FOUND, "Member does not exist"))
                }.flatMap {
                    memberRepo.deleteByUserIdAndProjectId(userId, projectId).thenReturn(userId)
                }
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
     * Update the roles of members within a given project.
     * Checks if the requester has the permission to do so.
     * @param projectId
     * @param requester
     * @param userId of user that should be updated
     * @param role new ProjectRole
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
            .flatMap {
                publishEventChangedMemberRole(projectId, it.second, it.first.projectRole, it.second.projectRole)
            }
    }

    /**
     * Checks if a user fulfills the soft permissions.
     * Are fulfilled if the user is the projectCreator, the user is project member or the user is a global admin
     * @throws ServiceException when user does not fulfill
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
     * Checks if a user fulfills the hard permissions.
     * Are fulfilled if the user is the projectCreator, the user is project admin or the user is a global admin
     * @throws ServiceException when user does not fulfill
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
                    projectRepo.findById(projectId)
                            .filter {
                                it.creatorId == user.id!! || user.globalRole == GlobalRole.ADMIN.name
                            }.map {
                                projectId
                            }
                }
            }.switchIfEmpty {
                Mono.error(ServiceException(HttpStatus.FORBIDDEN, "No permissions"))
            }
    }
}
