package de.thm.mni.microservices.gruppe6.project.controller

import de.thm.mni.microservices.gruppe6.lib.classes.userService.User
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@RestController
@CrossOrigin
@RequestMapping("/api/projects")
class ProjectController(@Autowired val projectService: ProjectDbService) {

    // toDo: remove when jwt works
    val jwtUser = User(
        UUID.fromString("a443ffd0-f7a8-44f6-8ad3-87acd1e91042")
        ,"Peter_Zwegat"
        ,"password"
        , "Peter"
        , "Zwegat"
        ,"peter.zwegat@mni.thm.de"
        , LocalDate.now()
        , LocalDateTime.now()
        ,"USER"
        ,null)

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
    @PostMapping("{projectName}")
    @ResponseStatus(value = HttpStatus.CREATED)
    fun createProject(@PathVariable projectName: String): Mono<Project> {
        return projectService.createProject(projectName, jwtUser.id!!)
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
