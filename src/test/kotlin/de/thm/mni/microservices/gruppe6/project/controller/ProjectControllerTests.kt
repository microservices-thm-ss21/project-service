package de.thm.mni.microservices.gruppe6.project.controller

import de.thm.mni.microservices.gruppe6.lib.classes.projectService.Project
import de.thm.mni.microservices.gruppe6.lib.classes.projectService.ProjectRole
import de.thm.mni.microservices.gruppe6.lib.classes.userService.User
import de.thm.mni.microservices.gruppe6.lib.exception.ServiceException
import de.thm.mni.microservices.gruppe6.project.model.persistence.ProjectRepository
import de.thm.mni.microservices.gruppe6.project.service.ProjectDbService
import org.junit.jupiter.api.BeforeEach
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
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [ProjectController::class])
@WithMockUser
class ProjectControllerTests {

    @Autowired private lateinit var webTestClient: WebTestClient
    @MockBean private lateinit var projectService: ProjectDbService
    @Mock private lateinit var projectRepository: ProjectRepository

    private val PROJECTS_URI = "/api/projects"

    private val mockedUser = User(UUID.fromString("a443ffd0-f7a8-44f6-8ad3-87acd1e91042"), "Peter_Zwegat", "password", "Peter", "Zwegat", "peter.zwegat@mni.thm.de", LocalDate.now(), LocalDateTime.now(), ProjectRole.ADMIN.name, null)

    @BeforeEach
    fun setUp() {
        webTestClient = webTestClient.mutateWith(csrf())
    }

    private fun createTestProject(name: String): Project {
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
        given(projectService.getProjectById(project.id!!)).willThrow(ServiceException(HttpStatus.NOT_FOUND))

        webTestClient
                .get()
                .uri("$PROJECTS_URI/${project.id}")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound

        verify(projectService, times(1)).getProjectById(project.id!!)
    }

    // does not work due to "Argument Type Mismatch" -> auth: ServiceAuthentication is passed as org.springframework.security.authentication.UsernamePasswordAuthenticationToken

//    @Test
//    fun testShouldDeleteProject() {
//        val projectId = UUID.randomUUID()
//        val requester = mockedUser
//        given(projectService.deleteProject(projectId, requester)).willReturn(Mono.just(projectId))
//
//        webTestClient
//                .delete()
//                .uri("$PROJECTS_URI/${projectId}")
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isNoContent
//                .expectBody().isEmpty
//
//        verify(projectService, times(1)).deleteProject(projectId, requester)
//    }

//    @Test
//    fun testShouldCreateProject() {
//        val project = createTestProject("project to be created")
//        val requester = mockedUser
//
//        given(projectService.createProject(project.name, requester)).willReturn(Mono.just(project))
//
//        webTestClient
//                .post()
//                .uri("$PROJECTS_URI/${project.name}")
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isCreated
//
//        verify(projectService, times(1)).createProject(project.name, requester)
//    }

//    @Test
//    fun testShouldNotCreateProject() {
//        val project = createTestProject("project to be created")
//        val requester = mockedUser
//
//        given(projectService.createProject(project.name, requester)).willReturn(Mono.error(ServiceException(HttpStatus.INTERNAL_SERVER_ERROR)))
//
//        webTestClient
//                .post()
//                .uri("$PROJECTS_URI/${project.name}")
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
//
//        verify(projectService, times(1)).createProject(project.name, requester)
//    }


//    @Test
//    fun testShouldUpdateProject() {
//        val project = createTestProject("project to be created")
//        val updatedProject = project.copy(name = "updated project")
//        val requester = mockedUser
//
//        given(projectService.updateProjectName(project.id!!, requester, updatedProject.name)).willReturn(Mono.just(updatedProject))
//
//        webTestClient
//                .put()
//                .uri("${PROJECTS_URI}/${project.id!!}/name/${updatedProject.name}")
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk
//                .expectBody()
//                .jsonPath("$", updatedProject)
//
//        verify(projectService, times(1)).updateProjectName(project.id!!, requester, updatedProject.name)
//    }

//    @Test
//    fun testShouldNotUpdateProject() {
//        val project = createTestProject("project to be created")
//        val updatedProject = project.copy(name = "updated project")
//        val requester = mockedUser
//
//        given(projectService.updateProjectName(project.id!!, requester, updatedProject.name)).willReturn(Mono.error(Throwable()))
//
//        webTestClient
//                .put()
//                .uri("${PROJECTS_URI}/${project.id!!}/name/${updatedProject.name}")
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
//
//        verify(projectService, times(1)).updateProjectName(project.id!!, requester, updatedProject.name)
//    }
}
