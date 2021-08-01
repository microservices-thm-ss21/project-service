package de.thm.mni.microservices.gruppe6.project.service

import de.thm.mni.microservices.gruppe6.lib.classes.projectService.Project
import de.thm.mni.microservices.gruppe6.lib.classes.projectService.ProjectRole
import de.thm.mni.microservices.gruppe6.lib.classes.userService.User
import de.thm.mni.microservices.gruppe6.lib.event.*
import de.thm.mni.microservices.gruppe6.project.model.persistence.ProjectRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.*

@Component
class ProjectDbService(
    @Autowired private val projectRepo: ProjectRepository,
    @Autowired private val memberDbService: MemberDbService,
    @Autowired private val sender: JmsTemplate
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * returns all stores projects
     */
    fun getAllProjects(): Flux<Project> {
        logger.debug("getAllProjects")
        return projectRepo.findAll()
    }

    /**
     * returns all stores projects that include the user as a member
     */
    fun getAllProjectsOfUser(userId: UUID): Flux<Project> {
        logger.debug("getAllProjectsOfUser $userId")
        return memberDbService.getAllProjectIdsOfMember(userId).flatMap { projectRepo.findById(it) }
    }

    /**
     * returns stored project
     */
    fun getProjectById(id: UUID): Mono<Project> {
        logger.debug("getProjectById $id")
        return projectRepo.findById(id)
    }

    @Transactional
    fun createProject(projectName: String, requester: User): Mono<Project> {
        logger.debug("getAllProjects")
        return projectRepo.save(Project(projectName, requester.id!!))
            .flatMap {
                memberDbService.createMember(it.id!!, requester, it.creatorId!!, ProjectRole.ADMIN)
                    .then(Mono.just(it))
            }
            .publishOn(Schedulers.boundedElastic()).map {
                sender.convertAndSend(
                    EventTopic.DataEvents.topic,
                    ProjectDataEvent(DataEventCode.CREATED, it.id!!)
                )
                it
            }
    }

    /**
     * Updates name of project
     * @param projectId: project id
     * @param requester
     * @param projectName
     */
    fun updateProjectName(projectId: UUID, requester: User, projectName: String): Mono<Project> {
        logger.debug("updateProjectName $projectId $requester $projectName")
        return memberDbService.checkSoftPermissions(projectId, requester)
            .flatMap { projectRepo.findById(projectId) }
            .flatMap { oldProject ->
                projectRepo.save(Project(oldProject.id!!, projectName, oldProject.creatorId, oldProject.createTime))
                    .map { newProject ->
                        Pair(oldProject, newProject)
                    }
            }
            .publishOn(Schedulers.boundedElastic()).map {
                sender.convertAndSend(
                    EventTopic.DataEvents.topic,
                    ProjectDataEvent(DataEventCode.UPDATED, projectId)
                )
                sender.convertAndSend(
                    EventTopic.DomainEvents_ProjectService.topic,
                    DomainEventChangedString(
                        DomainEventCode.PROJECT_CHANGED_NAME,
                        projectId,
                        it.first.name,
                        it.second.name
                    )
                )
                it.second
            }
    }

    /**
     * Deletes project by id
     * @param projectId: project id
     * @param user
     */
    fun deleteProject(projectId: UUID, requester: User): Mono<Void> {
        logger.debug("deleteProject $projectId $requester")
        return memberDbService.checkHardPermissions(projectId, requester)
            .flatMap {
                projectRepo.deleteById(projectId)
            }
            .publishOn(Schedulers.boundedElastic()).map {
                sender.convertAndSend(
                    EventTopic.DataEvents.topic,
                    ProjectDataEvent(DataEventCode.DELETED, projectId)
                )
                it
            }
    }
}
