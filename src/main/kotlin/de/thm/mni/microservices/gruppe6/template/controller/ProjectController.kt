package de.thm.mni.microservices.gruppe6.template.controller

import de.thm.mni.microservices.gruppe6.template.model.message.MemberDTO
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

    @GetMapping("/")
    fun getAllProjects(): Flux<Project> = projectService.getAllProjects()

    @PutMapping("/")
    fun putProject(@RequestBody projectDTO: ProjectDTO): Mono<Project> = projectService.putProject(projectDTO)

    @PostMapping("/{id}")
    fun updateProject(@PathVariable id: UUID, @RequestBody projectDTO: ProjectDTO) = projectService.updateProject(id, projectDTO)

    @DeleteMapping("/{id}")
    fun deleteProject(@PathVariable id: UUID) = projectService.deleteProject(id)

    @PutMapping("/{id}/members")
    fun putMembers(@PathVariable id: UUID, @RequestBody memberDTO: MemberDTO): Mono<Project> = projectService.putMembers(id, memberDTO)

    @GetMapping("/{id}/members")
    fun getMembers(@PathVariable id: UUID): Flux<Member> = projectService.getAllMembers(id)
}
