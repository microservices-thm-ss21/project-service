package de.thm.mni.microservices.gruppe6.template.controller

import de.thm.mni.microservices.gruppe6.template.model.message.ProjectDTO
import de.thm.mni.microservices.gruppe6.template.model.persistence.Project
import de.thm.mni.microservices.gruppe6.template.service.ProjectDbService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/projects")
class ProjectController(@Autowired val projectService: ProjectDbService) {

    @GetMapping("/")
    fun getAllProjects(): Flux<Project> = projectService.getAllProjects()

    @PutMapping("/")
    fun putProject(@RequestBody projectDTO: ProjectDTO): Mono<Project> = projectService.putProject(projectDTO)

    @PostMapping("/{id}")
    fun updateProject(@PathVariable id: Long, @RequestBody projectDTO: ProjectDTO) = projectService.updateProject(id, projectDTO)

    @DeleteMapping("/{id}")
    fun deleteProject(@PathVariable id: Long) = projectService.deleteProject(id)
}
