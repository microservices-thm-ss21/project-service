package de.thm.mni.microservices.gruppe6.project.controller

import de.thm.mni.microservices.gruppe6.lib.classes.authentication.ServiceAuthentication
import de.thm.mni.microservices.gruppe6.lib.classes.projectService.Project
import de.thm.mni.microservices.gruppe6.lib.exception.coverUnexpectedException
import de.thm.mni.microservices.gruppe6.project.service.ProjectDbService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
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
        .onErrorResume { Mono.error(coverUnexpectedException(it)) }

    /**
     * Returns all stored projects in which the user is included as a member
     */
    @GetMapping("user/{userId}")
    fun getAllProjectsOfUser(@PathVariable userId: UUID): Flux<Project> =
        projectService.getAllProjectsOfUser(userId)
            .onErrorResume { Mono.error(coverUnexpectedException(it)) }

    /**
     * Returns project with given id
     * @param projectId
     */
    @GetMapping("{projectId}")
    fun getProject(@PathVariable projectId: UUID): Mono<Project> =
        projectService.getProjectById(projectId)
            .onErrorResume { Mono.error(coverUnexpectedException(it)) }

    /**
     * Creates a new project with members
     */
    @PostMapping("{projectName}")
    @ResponseStatus(value = HttpStatus.CREATED)
    fun createProject(@PathVariable projectName: String, auth: ServiceAuthentication): Mono<Project> {
        return projectService.createProject(projectName, auth.user!!)
            .onErrorResume { Mono.error(coverUnexpectedException(it)) }
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
        .onErrorResume { Mono.error(coverUnexpectedException(it)) }

    /**
     * Deletes project with given id
     * @param projectId: project id
     */
    @DeleteMapping("{projectId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    fun deleteProject(@PathVariable projectId: UUID, auth: ServiceAuthentication): Mono<Void> =
        projectService.deleteProject(projectId, auth.user!!)
            .onErrorResume { Mono.error(coverUnexpectedException(it)) }
            .flatMap { Mono.empty() }

}
