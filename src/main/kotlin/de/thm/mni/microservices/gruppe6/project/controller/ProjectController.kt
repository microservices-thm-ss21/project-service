package de.thm.mni.microservices.gruppe6.project.controller

import de.thm.mni.microservices.gruppe6.lib.exception.ServiceException
import de.thm.mni.microservices.gruppe6.project.model.message.ProjectDTO
import de.thm.mni.microservices.gruppe6.project.model.persistence.Project
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
    @PutMapping("{id}")
    fun updateProject(@PathVariable id: UUID, @RequestBody projectDTO: ProjectDTO): Mono<Project> = projectService.updateProject(id, projectDTO).onErrorResume { Mono.error(ServiceException(HttpStatus.CONFLICT, "Either Project creator or Member(s) does not exist", it)) }

    /**
     * Deletes project with given id
     * @param id: project id
     */
    @DeleteMapping("{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    fun deleteProject(@PathVariable id: UUID): Mono<Void> = projectService.deleteProject(id)

}
