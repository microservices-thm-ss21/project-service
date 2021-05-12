package de.thm.mni.microservices.gruppe6.project.service

import de.thm.mni.microservices.gruppe6.project.model.message.ProjectDTO
import de.thm.mni.microservices.gruppe6.project.model.persistence.Project
import de.thm.mni.microservices.gruppe6.project.model.persistence.ProjectRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.*
import org.mockito.Mock;
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class ProjectDbServiceTests(
        @Mock private val projectRepository: ProjectRepository,
        @Mock private val memberService: MemberDbService
) {

    private val projectService = ProjectDbService(projectRepository, memberService)

    fun createTestProject(name: String): Project {
        return Project(UUID.randomUUID(), name, UUID.randomUUID(), LocalDateTime.now())
    }

    @Test
    fun testShouldReturnEmptyListOfProjects() {
        given(projectRepository.findAll()).willReturn(Flux.fromIterable(emptyList()))
        val projects: List<Project>? = projectService.getAllProjects().collectList().block()

        assertThat(projects).isNotNull
        assertThat(projects).isEmpty()
        assertThat(projects).isEqualTo(emptyList<Project>())

        verify(projectRepository, times(1)).findAll()
    }

    @Test
    fun testShouldReturnAllProjects() {
        val project1 = createTestProject("first project")
        val project2 = createTestProject("first project")
        val project3 = createTestProject("first project")
        val projectList = listOf(project1, project2, project3)

        given(projectRepository.findAll()).willReturn(Flux.fromIterable(projectList))
        val projects: List<Project>? = projectService.getAllProjects().collectList().block()

        assertThat(projects).`as`("list of projects").isNotNull
        assertThat(projects).`as`("list of projects").hasSize(projectList.size)
        projects!!.withIndex().forEach { assertThat(it.value).`as`("project $it.index").isEqualTo(projectList[it.index]) }

        Mockito.verify(projectRepository, times(1)).findAll()
    }

    @Test
    fun testShouldReturnProject() {
        val project = createTestProject("project to return")

        given(projectRepository.findById(project.id!!)).willReturn(Mono.just(project))

        val returnedProject: Project? = projectService.getProjectById(project.id!!).block()
        assertThat(returnedProject).isNotNull
        assertThat(returnedProject).isEqualTo(project)

        verify(projectRepository, times(1)).findById(project.id!!)
    }

    @Test
    fun testShouldCreateProject() {
        val project = createTestProject("project to create")
        project.id = null
        val projectDTO = ProjectDTO()
        projectDTO.name = project.name
        projectDTO.creatorId = project.creatorId
        projectDTO.members = emptyList()
        val createdProject = project.copy(UUID.randomUUID())

        given(projectRepository.save(any())).willReturn(Mono.just(createdProject))
        val mockID = UUID.randomUUID()
        given(memberService.createMembers(mockID, emptyList())).willReturn(Flux.empty())
        val returnedProject: Project? = projectService.createProjectWithMembers(projectDTO).block()

        assertThat(returnedProject).`as`("created project").isNotNull
        assertThat(returnedProject).`as`("created project").isEqualTo(createdProject)

        verify(projectRepository, times(1)).save(any())
        verify(memberService, times(0)).createMembers(mockID, emptyList())
    }

    @Test
    fun shouldUpdateProject() {
        val project = createTestProject("project to update")
        val updatedProject = project.copy(name = "updated project")
        val projectDTO = ProjectDTO()
        projectDTO.name = updatedProject.name
        projectDTO.creatorId = updatedProject.creatorId
        projectDTO.members = emptyList()

        given(projectRepository.findById(project.id!!)).willReturn(Mono.just(project))
        given(projectRepository.save(updatedProject)).willReturn(Mono.just(updatedProject))

        val returnedProject: Project? = projectService.updateProject(project.id!!, projectDTO).block()
        assertThat(returnedProject).`as`("updated project").isNotNull
        assertThat(returnedProject).`as`("updated project").isEqualTo(updatedProject)

        verify(projectRepository, times(1)).findById(project.id!!)
        verify(projectRepository, times(1)).save(updatedProject)
    }

    @Test
    fun shouldNotUpdateProject() {
        val project = createTestProject("project to update")
        val updatedProject = project.copy(name = "updated project")
        val projectDTO = ProjectDTO()
        projectDTO.name = updatedProject.name
        projectDTO.creatorId = updatedProject.creatorId
        projectDTO.members = emptyList()

        given(projectRepository.findById(project.id!!)).willReturn(Mono.empty())
        given(projectRepository.save(updatedProject)).willReturn(Mono.empty())

        val returnedProject: Project? = projectService.updateProject(project.id!!, projectDTO).block()
        assertThat(returnedProject).`as`("updated project").isNull()

        verify(projectRepository, times(1)).findById(project.id!!)
        verify(projectRepository, times(0)).save(updatedProject)
    }

    @Test
    fun shouldDeleteProject() {
        val id = UUID.randomUUID()

        given(projectRepository.deleteById(id)).willReturn(Mono.empty())

        projectService.deleteProject(id).block()

        verify(projectRepository, times(1)).deleteById(id)
    }

}
