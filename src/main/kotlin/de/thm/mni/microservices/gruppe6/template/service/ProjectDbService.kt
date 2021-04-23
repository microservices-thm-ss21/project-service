package de.thm.mni.microservices.gruppe6.template.service

import de.thm.mni.microservices.gruppe6.template.model.message.ProjectDTO
import de.thm.mni.microservices.gruppe6.template.model.persistence.Member
import de.thm.mni.microservices.gruppe6.template.model.persistence.MemberRepository
import de.thm.mni.microservices.gruppe6.template.model.persistence.Project
import de.thm.mni.microservices.gruppe6.template.model.persistence.ProjectRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Component
class ProjectDbService(@Autowired val projectRepo: ProjectRepository, @Autowired val memberRepo: MemberRepository) {

    /**
     * returns all stores projects
     */
    fun getAllProjects(): Flux<Project> = projectRepo.findAll()

    /**
     * creates new project and stores all given members
     */
    fun putProject(projectDTO: ProjectDTO): Mono<Project> {
        val project = projectRepo.save(Project(null, projectDTO))
        project.map {
            it.id?.let { id -> putMembers(id, projectDTO) }
        }
        return project
    }

    /**
     * Updates project
     * @param id: project id
     */
    fun updateProject(id: UUID, projectDTO: ProjectDTO): Mono<Project> {
        val project = projectRepo.findById(id)
        return project.map { it.applyProjectDTO(projectDTO) }
    }

    /**
     * Deletes project by id
     * @param id: project id
     */
    fun deleteProject(id: UUID): Mono<Void> {
        return projectRepo.deleteById(id)
    }

    /**
     * Gets all Members of a given Project id
     * @param id: project id
     */
    fun getMembers(id: UUID): Flux<Member> = memberRepo.findAll().filter { it.project_id == id }

    /**
     * Stores all given members
     * @param id: project id
     */
    fun putMembers(id: UUID, projectDTO: ProjectDTO): Mono<Project> {
        projectDTO.members?.forEach { m ->
            memberRepo.save(m)
        }
        return projectRepo.findById(id)
    }

    /**
     * @toDo Not implemented
     */
    fun deleteMembers(id: UUID, projectDTO: ProjectDTO): Mono<Void> {
        return memberRepo.deleteById(id)
    }

    /**
     * @toDo Not implemented
     */
    fun updateMembers(id: UUID, projectDTO: ProjectDTO): Flux<Member> {
        return memberRepo.findAll()
    }

    fun Project.applyProjectDTO(projectDTO: ProjectDTO): Project {
        this.name = projectDTO.name!!
        this.creator_id = projectDTO.creator_id!!
        return this
    }
}
