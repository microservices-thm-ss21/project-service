package de.thm.mni.microservices.gruppe6.project.service

import de.thm.mni.microservices.gruppe6.project.model.message.MemberDTO
import de.thm.mni.microservices.gruppe6.project.model.message.ProjectDTO
import de.thm.mni.microservices.gruppe6.project.model.persistence.Member
import de.thm.mni.microservices.gruppe6.project.model.persistence.MemberRepository
import de.thm.mni.microservices.gruppe6.project.model.persistence.Project
import de.thm.mni.microservices.gruppe6.project.model.persistence.ProjectRepository
import org.springframework.beans.factory.annotation.Autowired
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
    fun createProjectWithMembers(projectDTO: ProjectDTO): Mono<Project> {
        val project = projectRepo.save(Project(projectDTO))
        return project.doOnNext { createMembers(it.id!!, projectDTO.members) }
    }

    /**
     * Updates project
     * @param id: project id
     */
    fun updateProject(id: UUID, projectDTO: ProjectDTO): Mono<Project> {
        val project = projectRepo.findById(id)
        return project.map { it.applyProjectDTO(projectDTO) }.flatMap { projectRepo.save(it) }
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
    fun getMembers(id: UUID): Flux<Member> = memberRepo.getMembersByProjectID(id)

    /**
     * Stores all given members
     * @param id: project id
     * @toDo: Return value not implemented
     */
    fun createMembers(project_id: UUID, members: List<MemberDTO>?): Flux<Member> {
        if (members != null) {
            return Flux.fromIterable(members).flatMap { memberDTO -> memberRepo.save(Member(project_id, memberDTO)) }
        }
        return Flux.empty()
    }

    /**
     * Delete all members of a project given its id
     * @param id: project id
     */
    fun deleteAllMembers(id: UUID): Mono<Void> {
        return memberRepo.deleteAllMembersByProjectID(id)
    }

    /**
     * Delete given members of a project given its id
     * @param id: project id
     */
    fun deleteMembers(id: UUID, members: List<Member>): Mono<Void> {
        return memberRepo.deleteMembersByProjectID(id, members.map { m -> m.userId })
    }

    /**
     * Update the roles of members within a given project
     * @param id: project id
     */
    fun updateMemberRoles(id: UUID, members: List<MemberDTO>): Flux<Member> {
        return Flux.fromIterable(members).flatMap { memberRepo.findMemberOfProject(id, it.userId!!) }.zipWithIterable(members).flatMap { memberRepo.save(Member(it.t1.id, it.t1.projectId, it.t1.userId, it.t2.projectRole!!)) }
    }

    fun Project.applyProjectDTO(projectDTO: ProjectDTO): Project {
        this.name = projectDTO.name!!
        this.creatorId = projectDTO.creatorId!!
        return this
    }
}
