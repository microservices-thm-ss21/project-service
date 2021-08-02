package de.thm.mni.microservices.gruppe6.project.controller

import de.thm.mni.microservices.gruppe6.lib.classes.authentication.ServiceAuthentication
import de.thm.mni.microservices.gruppe6.lib.classes.projectService.Project
import de.thm.mni.microservices.gruppe6.lib.exception.ServiceException
import de.thm.mni.microservices.gruppe6.project.service.ProjectDbService
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
    fun getProject(@PathVariable id: UUID): Mono<Project> =
        projectService.getProjectById(id).switchIfEmpty { Mono.error(ServiceException(HttpStatus.NOT_FOUND)) }

    /**
     * Creates a new project with members
     */
    @PostMapping("{projectName}")
    @ResponseStatus(value = HttpStatus.CREATED)
    fun createProject(@PathVariable projectName: String, auth: ServiceAuthentication): Mono<Project> {
        return projectService.createProject(projectName, auth.user!!)
    }

    /**
     * Updates project details with given id
     * @param projectId: project id
     * @param projectName: project name
     */
    @PutMapping("/{projectId}/name/{projectName}")
    fun updateProjectName(
        @PathVariable projectId: UUID,
        @PathVariable projectName: String,
        auth: ServiceAuthentication
    ): Mono<Project> = projectService.updateProjectName(projectId, auth.user!!, projectName)

    /**
     * Deletes project with given id
     * @param projectId: project id
     */
    @DeleteMapping("{projectId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    fun deleteProject(@PathVariable projectId: UUID, auth: ServiceAuthentication): Mono<Void> =
        projectService.deleteProject(projectId, auth.user!!)

}
