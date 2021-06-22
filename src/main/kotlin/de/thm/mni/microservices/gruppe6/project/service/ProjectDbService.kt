package de.thm.mni.microservices.gruppe6.project.service

import de.thm.mni.microservices.gruppe6.lib.event.*
import de.thm.mni.microservices.gruppe6.project.model.message.ProjectDTO
import de.thm.mni.microservices.gruppe6.project.model.persistence.Project
import de.thm.mni.microservices.gruppe6.project.model.persistence.ProjectRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component
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

    /**
     * returns all stores projects
     */
    fun getAllProjects(): Flux<Project> = projectRepo.findAll()

    /**
     * returns all stores projects that include the user as a member
     */
    fun getAllProjectsOfUser(userId: UUID): Flux<Project> = projectRepo.findAllById(memberDbService.getAllProjectIdsOfMember(userId))

    /**
     * returns stored project
     */
    fun getProjectById(id: UUID): Mono<Project> = projectRepo.findById(id)

    /**
     * creates new project and stores all given members
     */
    fun createProjectWithMembers(projectDTO: ProjectDTO): Mono<Project> {
        val project = projectRepo.save(Project(projectDTO))
            .publishOn(Schedulers.boundedElastic()).map {
                sender.convertAndSend(
                    EventTopic.DataEvents.topic,
                    ProjectDataEvent(DataEventCode.CREATED, it.id!!))
                it
            }

        return project.map {
            memberDbService.createMembers(it.id!!, projectDTO.members).subscribe()
            it
        }
    }

    /**
     * Updates project
     * @param id: project id
     */
    fun updateProject(projectId: UUID, projectDTO: ProjectDTO): Mono<Project> {
        return projectRepo.findById(projectId)
            .map { it.applyProjectDTO(projectDTO) }
            .map { projectRepo.save(it.first)
            it}
            .publishOn(Schedulers.boundedElastic()).map {
                sender.convertAndSend(
                    EventTopic.DataEvents.topic,
                    ProjectDataEvent(DataEventCode.UPDATED, projectId))
                it.second.forEach{(topic, event) -> sender.convertAndSend(topic, event)}
                it.first
            }
    }

    /**
     * Deletes project by id
     * @param id: project id
     */
    fun deleteProject(projectId: UUID): Mono<Void> {
        return projectRepo.deleteById(projectId)
            .publishOn(Schedulers.boundedElastic()).map {
                sender.convertAndSend(
                    EventTopic.DataEvents.topic,
                    ProjectDataEvent(DataEventCode.DELETED, projectId))
                it
            }
    }

    /**
     * apply the projectDTO to the Project model as stored in DB and generate Domain Events
     * @param projectDTO request body to apply to a project
     * @return the updated project and a list of events to be issued: (Topic, new DomainEvent)
     */
    fun Project.applyProjectDTO(projectDTO: ProjectDTO): Pair<Project, List<Pair<String, DomainEvent>>> {
        val eventList = ArrayList<Pair<String, DomainEvent>>()

        if (this.name != projectDTO.name) {
            eventList.add(
                Pair(EventTopic.DomainEvents_ProjectService.topic,
                    DomainEventChangedString(
                        DomainEventCode.PROJECT_CHANGED_NAME,
                        this.id!!,
                        this.name,
                        projectDTO.name
                    )
                )
            )
            this.name = projectDTO.name!!
        }
        return Pair(this, eventList)
    }
}
