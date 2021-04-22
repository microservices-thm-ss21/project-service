package de.thm.mni.microservices.gruppe6.template.service


import de.thm.mni.microservices.gruppe6.template.model.message.ProjectDTO
import de.thm.mni.microservices.gruppe6.template.model.persistence.Project
import de.thm.mni.microservices.gruppe6.template.model.persistence.ProjectRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Component
class ProjectDbService(@Autowired val projectRepo: ProjectRepository) {
    fun getAllProjects(): Flux<Project> = projectRepo.findAll()

    fun putProject(projectDTO: ProjectDTO): Mono<Project> {
        return projectRepo.save(Project(null, projectDTO))
    }

    fun updateProject(id: UUID, projectDTO: ProjectDTO): Mono<Project> {
        val project = projectRepo.findById(id)
        return project.map { it.applyProjectDTO(projectDTO) }
    }

    fun deleteProject(id: UUID): Mono<Void> {
        return projectRepo.deleteById(id)
    }

    fun Project.applyProjectDTO(projectDTO: ProjectDTO): Project {
        this.name = projectDTO.name!!
        return this
    }
}
