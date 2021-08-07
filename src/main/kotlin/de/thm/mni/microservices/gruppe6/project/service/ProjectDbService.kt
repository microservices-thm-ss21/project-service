package de.thm.mni.microservices.gruppe6.project.service

import de.thm.mni.microservices.gruppe6.lib.classes.projectService.Project
import de.thm.mni.microservices.gruppe6.lib.classes.projectService.ProjectRole
import de.thm.mni.microservices.gruppe6.lib.classes.userService.User
import de.thm.mni.microservices.gruppe6.lib.event.*
import de.thm.mni.microservices.gruppe6.lib.exception.ServiceException
import de.thm.mni.microservices.gruppe6.project.model.persistence.ProjectRepository
import de.thm.mni.microservices.gruppe6.project.requests.Requester
import de.thm.mni.microservices.gruppe6.project.saga.service.ProjectDeletedSagaService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.switchIfEmpty
import java.util.*

/**
 * Implements the functionality to handle projects.
 */
@Component
class ProjectDbService(
    @Autowired private val projectRepo: ProjectRepository,
    @Autowired private val memberDbService: MemberDbService,
    @Autowired private val sender: JmsTemplate,
    @Autowired private val httpRequester: Requester,
    @Autowired private val projectDeletedSagaService: ProjectDeletedSagaService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Returns all stores projects
     * @return flux of projects
     */
    fun getAllProjects(): Flux<Project> {
        logger.debug("getAllProjects")
        return projectRepo.findAll()
    }

    /**
     * Returns all stores projects that include the user as a member
     * @return flux of projects
     */
    fun getAllProjectsOfUser(userId: UUID): Flux<Project> {
        logger.debug("getAllProjectsOfUser $userId")
        return memberDbService.getAllProjectIdsOfMember(userId).flatMap { projectRepo.findById(it) }
    }

    /**
     * Returns stored project with given id
     * @param projectId
     * @throws ServiceException when the project does not exist
     * @return mono of a project
     */
    fun getProjectById(projectId: UUID): Mono<Project> {
        logger.debug("getProjectById $projectId")
        return projectRepo.findById(projectId).switchIfEmpty { Mono.error(ServiceException(HttpStatus.NOT_FOUND)) }
    }

    /**
     * Creates a project and adds the requester as the first member with admin rights.
     * Sends all necessary events.
     * @param projectName
     * @param requester
     * @return mono of the new project
     */
    @Transactional
    fun createProject(projectName: String, requester: User): Mono<Project> {
        logger.debug("createProject $projectName $requester")
        return projectRepo.save(Project(projectName, requester.id!!))
            .flatMap {
                memberDbService.addMember(it.id!!, requester, it.creatorId!!, ProjectRole.ADMIN)
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
     * Updates name of project. Checks requester permissions first and sends all necessary events.
     * @param projectId
     * @param requester
     * @param projectName the new project name
     * @throws ServiceException if permissions are not fulfilled or project not existing
     * @return updated project
     */
    fun updateProjectName(projectId: UUID, requester: User, projectName: String): Mono<Project> {
        logger.debug("updateProjectName $projectId $requester $projectName")
        return memberDbService.checkSoftPermissions(projectId, requester)
            .flatMap { projectRepo.findById(projectId) }
            .switchIfEmpty { Mono.error(ServiceException(HttpStatus.NOT_FOUND)) }
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
     * Deletes project by id. Checks requester permissions first and sends all necessary events.
     * @param projectId
     * @param requester
     * @throws ServiceException if project does not exist
     * @return id of the deleted project
     */
    fun deleteProject(projectId: UUID, requester: User): Mono<UUID> {
        logger.debug("deleteProject $projectId $requester")
        return memberDbService.checkHardPermissions(projectId, requester)
            .flatMap {
                projectRepo.findById(projectId)
            }.switchIfEmpty {
                Mono.error(ServiceException(HttpStatus.NOT_FOUND, "Project does not exist"))
            }.doOnNext {
                projectRepo.deleteById(it.id!!).thenReturn(it.id!!)
            }.doOnNext(projectDeletedSagaService::startSaga)
            .publishOn(Schedulers.boundedElastic())
            .doOnNext {
                sender.convertAndSend(
                    EventTopic.DataEvents.topic,
                    ProjectDataEvent(DataEventCode.DELETED, projectId)
                )
            }.thenReturn(projectId)

    }
}
