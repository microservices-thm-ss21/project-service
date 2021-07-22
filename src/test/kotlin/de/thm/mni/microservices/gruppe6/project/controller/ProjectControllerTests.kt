package de.thm.mni.microservices.gruppe6.project.controller

import de.thm.mni.microservices.gruppe6.lib.exception.ServiceException
import de.thm.mni.microservices.gruppe6.project.model.message.ProjectDTO
import de.thm.mni.microservices.gruppe6.project.model.persistence.Project
import de.thm.mni.microservices.gruppe6.project.model.persistence.ProjectRepository
import de.thm.mni.microservices.gruppe6.project.service.ProjectDbService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [ProjectController::class])
class ProjectControllerTests {

    @Autowired private lateinit var webTestClient: WebTestClient
    @MockBean private lateinit var projectService: ProjectDbService
    @Mock private lateinit var projectRepository: ProjectRepository

    private val PROJECTS_URI = "/api/projects"

    fun createTestProject(name: String): Project {
        return Project(UUID.randomUUID(), name, UUID.randomUUID(), LocalDateTime.now())
    }

    @Test
    fun testShouldGetEmptyListOfProjects() {
        val projects = emptyList<Project>()
        given(projectService.getAllProjects()).willReturn(Flux.fromIterable(projects))

        webTestClient
                .get()
                .uri(PROJECTS_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody().jsonPath("$", projects)

        verify(projectService, times(1)).getAllProjects()
    }

    @Test
    fun testShouldGetAllProjects() {
        val projects = listOf(createTestProject("first project"), createTestProject("second project"), createTestProject("third project"))
        given(projectService.getAllProjects()).willReturn(Flux.fromIterable(projects))

        webTestClient
                .get()
                .uri(PROJECTS_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody().jsonPath("$", projects)

        verify(projectService, times(1)).getAllProjects()
    }

    @Test
    fun testShouldGetProject() {
        val project = createTestProject("test project")
        given(projectService.getProjectById(project.id!!)).willReturn(Mono.just(project))

        webTestClient
                .get()
                .uri("$PROJECTS_URI/${project.id}")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody().jsonPath("$", project)

        verify(projectService, times(1)).getProjectById(project.id!!)
    }

    @Test
    fun testShouldNotGetProject() {
        val project = createTestProject("test project")
        given(projectService.getProjectById(project.id!!)).willReturn(Mono.empty())

        webTestClient
                .get()
                .uri("$PROJECTS_URI/${project.id}")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound

        verify(projectService, times(1)).getProjectById(project.id!!)
    }
/*
    @Test
    fun testShouldDeleteProject() {
        val projectId = UUID.randomUUID()
        given(projectService.deleteProject(projectId)).willReturn(Mono.empty())

        webTestClient
                .delete()
                .uri("$PROJECTS_URI/${projectId}")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent
                .expectBody().isEmpty

        verify(projectService, times(1)).deleteProject(projectId)
    }
*/
    @Test
    fun testShouldCreateProject() {
        val project = createTestProject("project to be created")
        val projectDTO = ProjectDTO()
        projectDTO.name = project.name
        projectDTO.creatorId = project.creatorId
        projectDTO.members = emptyList()

        given(projectService.createProjectWithMembers(projectDTO)).willReturn(Mono.just(project))

        webTestClient
                .post()
                .uri(PROJECTS_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(projectDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated

        verify(projectService, times(1)).createProjectWithMembers(projectDTO)
    }

    @Test
    fun testShouldNotCreateProject() {
        val project = createTestProject("project to be created")
        val projectDTO = ProjectDTO()
        projectDTO.name = project.name
        projectDTO.creatorId = project.creatorId
        projectDTO.members = emptyList()

        given(projectService.createProjectWithMembers(projectDTO)).willReturn(Mono.error(ServiceException(HttpStatus.BAD_REQUEST, "Members must not be empty/null")))

        webTestClient
                .post()
                .uri(PROJECTS_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(projectDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)

        verify(projectService, times(1)).createProjectWithMembers(projectDTO)
    }
/*
    @Test
    fun testShouldUpdateProject() {
        val project = createTestProject("project to be created")
        val projectDTO = ProjectDTO()
        projectDTO.name = project.name
        projectDTO.creatorId = project.creatorId
        projectDTO.members = emptyList()
        val updatedProject = project.copy(name = "updated project")

        given(projectService.updateProject(project.id!!, projectDTO)).willReturn(Mono.just(updatedProject))

        webTestClient
                .put()
                .uri("${PROJECTS_URI}/${project.id!!}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(projectDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$", updatedProject)

        verify(projectService, times(1)).updateProject(project.id!!, projectDTO)
    }
*/
    /*
    @Test
    fun testShouldNotUpdateProject() {
        val project = createTestProject("project to be created")
        val projectDTO = ProjectDTO()
        projectDTO.name = project.name
        projectDTO.creatorId = project.creatorId
        projectDTO.members = emptyList()

        given(projectService.updateProject(project.id!!, projectDTO)).willReturn(Mono.error(Throwable()))

        webTestClient
                .put()
                .uri("${PROJECTS_URI}/${project.id!!}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(projectDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)

        verify(projectService, times(1)).updateProject(project.id!!, projectDTO)
    }
*/
}
