package de.thm.mni.microservices.gruppe6.project.controller

import de.thm.mni.microservices.gruppe6.lib.exception.ServiceException
import de.thm.mni.microservices.gruppe6.project.model.message.ProjectDTO
import de.thm.mni.microservices.gruppe6.project.model.persistence.Project
import de.thm.mni.microservices.gruppe6.project.service.ProjectDbService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.util.*

@RestController
@CrossOrigin
@RequestMapping("/api/projects")
class ProjectController(@Autowired val projectService: ProjectDbService) {

    /**
     * Returns all stored projects
     */
    @GetMapping("")
    fun getAllProjects(): Flux<Project> = projectService.getAllProjects()

    /**
     * Returns all stored projects in which the user is included as a member
     */
    @GetMapping("user/{userId}")
    fun getAllProjectsOfUser(@PathVariable userId: UUID): Flux<Project> = projectService.getAllProjectsOfUser(userId)

    /**
     * Returns project with given id
     * @param id: project id
     */
    @GetMapping("{id}")
    fun getProject(@PathVariable id: UUID): Mono<Project> = projectService.getProjectById(id).switchIfEmpty { Mono.error(ServiceException(HttpStatus.NOT_FOUND)) }

    /**
     * Creates a new project with members
     */
    @PostMapping("")
    @ResponseStatus(value = HttpStatus.CREATED)
    fun createProject(@RequestBody projectDTO: ProjectDTO): Mono<Project> {
        val project = projectService.createProjectWithMembers(projectDTO)
        return project.onErrorResume { Mono.error(ServiceException(httpStatus = HttpStatus.CONFLICT, cause = it)) }
    }

    /**
     * Updates project details with given id
     * @param id: project id
     */
    @PutMapping("/{projectId}/user/{userId}")
    fun updateProject(@PathVariable projectId: UUID, @PathVariable userId: UUID, @RequestBody projectDTO: ProjectDTO): Mono<Project> = projectService.updateProject(projectId, userId, projectDTO)

    /**
     * Deletes project with given id
     * @param id: project id
     */
    @DeleteMapping("{projectId}/user/{userId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    fun deleteProject(@PathVariable projectId: UUID, @PathVariable userId: UUID): Mono<Void> = projectService.deleteProject(projectId, userId)

}
