package de.thm.mni.microservices.gruppe6.template.controller

import de.thm.mni.microservices.gruppe6.template.model.message.ProjectDTO
import de.thm.mni.microservices.gruppe6.template.model.persistence.Member
import de.thm.mni.microservices.gruppe6.template.model.persistence.Project
import de.thm.mni.microservices.gruppe6.template.service.ProjectDbService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/api/projects")
class ProjectController(@Autowired val projectService: ProjectDbService) {

    /**
     * Returns all stored projects
     */
    @GetMapping("/")
    fun getAllProjects(): Flux<Project> = projectService.getAllProjects()

    /**
     * Creates a new project with members
     */
    @PutMapping("/")
    fun putProject(@RequestBody projectDTO: ProjectDTO): Mono<Project> = projectService.putProject(projectDTO)

    /**
     * Updates project details with given id
     * @param id: project id
     */
    @PostMapping("/{id}")
    fun updateProject(@PathVariable id: UUID, @RequestBody projectDTO: ProjectDTO) = projectService.updateProject(id, projectDTO)

    /**
     * Deletes project with given id
     * @param id: project id
     */
    @DeleteMapping("/{id}")
    fun deleteProject(@PathVariable id: UUID) = projectService.deleteProject(id)

    /**
     * Creates new members for a project with given id
     * @param id: project id
     */
    @PutMapping("/{id}/members")
    fun putMembers(@PathVariable id: UUID, @RequestBody projectDTO: ProjectDTO): Mono<Project> = projectService.putMembers(id, projectDTO)

    /**
     * Get all members of a given project
     * @param id: project id
     */
    @GetMapping("/{id}/members")
    fun getMembers(@PathVariable id: UUID): Flux<Member> = projectService.getMembers(id)

    /**
     * Deletes all given members of given project
     * @param id: project id
     */
    @DeleteMapping("/{id}")
    fun deleteMembers(@PathVariable id: UUID, @RequestBody projectDTO: ProjectDTO): Mono<Void> = projectService.deleteMembers(id, projectDTO)


    @PutMapping("/{id}")
    fun updateMembers(@PathVariable id: UUID, @RequestBody projectDTO: ProjectDTO): Flux<Member> = projectService.updateMembers(id, projectDTO)
}
