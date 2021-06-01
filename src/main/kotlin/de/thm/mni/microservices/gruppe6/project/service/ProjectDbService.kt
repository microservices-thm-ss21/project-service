package de.thm.mni.microservices.gruppe6.project.service

import de.thm.mni.microservices.gruppe6.project.model.message.ProjectDTO
import de.thm.mni.microservices.gruppe6.project.model.persistence.Project
import de.thm.mni.microservices.gruppe6.project.model.persistence.ProjectRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Component
class ProjectDbService(@Autowired private val projectRepo: ProjectRepository, @Autowired private val memberDbService: MemberDbService) {

    /**
     * returns all stores projects
     */
    fun getAllProjects(): Flux<Project> = projectRepo.findAll()

    /**
     * returns stored project
     */
    fun getProjectById(id: UUID): Mono<Project> = projectRepo.findById(id)

    /**
     * creates new project and stores all given members
     */
    fun createProjectWithMembers(projectDTO: ProjectDTO): Mono<Project> {
        val tst = Project(projectDTO)
        val project = projectRepo.save(tst)
        return project.doOnNext { memberDbService.createMembers(it.id!!, projectDTO.members) }
    }

    /**
     * Updates project
     * @param id: project id
     */
    fun updateProject(id: UUID, projectDTO: ProjectDTO): Mono<Project> {
        val project = projectRepo.findById(id)
        return project.map { it.applyProjectDTO(projectDTO) }.flatMap { projectRepo.save(it) }
    }

    /**
     * Deletes project by id
     * @param id: project id
     */
    fun deleteProject(id: UUID): Mono<Void> {
        return projectRepo.deleteById(id)
    }

    fun Project.applyProjectDTO(projectDTO: ProjectDTO): Project {
        this.name = projectDTO.name!!
        this.creatorId = projectDTO.creatorId!!
        return this
    }
}
