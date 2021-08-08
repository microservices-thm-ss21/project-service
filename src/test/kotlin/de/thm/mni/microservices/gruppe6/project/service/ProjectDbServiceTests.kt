package de.thm.mni.microservices.gruppe6.project.service

import de.thm.mni.microservices.gruppe6.lib.classes.projectService.Member
import de.thm.mni.microservices.gruppe6.lib.classes.projectService.Project
import de.thm.mni.microservices.gruppe6.lib.classes.projectService.ProjectRole
import de.thm.mni.microservices.gruppe6.lib.classes.userService.User
import de.thm.mni.microservices.gruppe6.lib.exception.ServiceException
import de.thm.mni.microservices.gruppe6.project.model.persistence.ProjectRepository
import de.thm.mni.microservices.gruppe6.project.saga.service.ProjectDeletedSagaService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.jms.core.JmsTemplate
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class ProjectDbServiceTests(
    @Mock private val projectRepository: ProjectRepository,
    @Mock private val memberService: MemberDbService,
    @Mock private val sender: JmsTemplate,
    @Mock private val projectDeletedSagaService: ProjectDeletedSagaService
) {

    private val projectService = ProjectDbService(projectRepository, memberService, sender, projectDeletedSagaService)

    private fun createTestProject(name: String): Project {
        return Project(UUID.randomUUID(), name, UUID.randomUUID(), LocalDateTime.now())
    }

    private fun createTestUser(): User {
        return User(
            UUID.randomUUID(), "username", "Password", "name", "lastName", "email",
            LocalDate.now(), LocalDateTime.now(), "ADMIN"
        )
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
        projects!!.withIndex()
            .forEach { assertThat(it.value).`as`("project $it.index").isEqualTo(projectList[it.index]) }

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
        val user = createTestUser()
        project.creatorId = user.id
        val member = Member(UUID.randomUUID(), project.id!!, user.id!!, "ADMIN")

        given(projectRepository.save(any())).willReturn(Mono.just(project))
        given(memberService.addMember(project.id!!, user, user.id!!, ProjectRole.ADMIN)).willReturn(Mono.just(member))

        val returnedProject = projectService.createProject(project.name, user).block()

        assertThat(returnedProject).`as`("created project").isNotNull
        assertThat(returnedProject).`as`("created project").isEqualTo(project)
    }

    @Test
    fun shouldUpdateProjectName() {
        val project = createTestProject("project to update")
        val user = createTestUser()
        given(memberService.checkSoftPermissions(project.id!!, user)).willReturn(Mono.just(project.id!!))
        given(projectRepository.findById(project.id!!)).willReturn(Mono.just(project))
        given(projectRepository.save(project)).willReturn(Mono.just(project))

        val returnedProject = projectService.updateProjectName(project.id!!, user, project.name).block()
        assertThat(returnedProject).`as`("updated project").isNotNull
        assertThat(returnedProject).`as`("updated project").isEqualTo(project)

        verify(projectRepository, times(1)).findById(project.id!!)
        verify(projectRepository, times(1)).save(project)
    }

    @Test
    fun shouldNotUpdateProject1() {
        val project = createTestProject("project to update")
        val user = createTestUser()

        given(
            memberService.checkSoftPermissions(
                project.id!!,
                user
            )
        ).willReturn(Mono.error(ServiceException(HttpStatus.FORBIDDEN)))
        given(projectRepository.findById(project.id!!)).willReturn(Mono.empty())
        given(projectRepository.save(project)).willReturn(Mono.error(Throwable()))
        try {
            projectService.updateProjectName(project.id!!, user, project.name).block()
        } catch (e: Throwable) {
            assertThat(e is ServiceException)
            assertThat((e as ServiceException).status.value() == HttpStatus.FORBIDDEN.value())
        }

        verify(memberService, times(1)).checkSoftPermissions(project.id!!, user)
        verify(projectRepository, times(0)).findById(project.id!!)
        verify(projectRepository, times(0)).save(project)
    }

    @Test
    fun shouldNotUpdateProject2() {
        val project = createTestProject("project to update")
        val user = createTestUser()

        given(memberService.checkSoftPermissions(project.id!!, user)).willReturn(Mono.just(project.id!!))
        given(projectRepository.findById(project.id!!)).willReturn(Mono.empty())
        given(projectRepository.save(project)).willReturn(Mono.error(Throwable()))

        try {
            projectService.updateProjectName(project.id!!, user, project.name).block()
        } catch (e: Throwable) {
            assertThat(e is ServiceException)
            assertThat((e as ServiceException).status.value() == HttpStatus.NOT_FOUND.value())
        }

        verify(memberService, times(1)).checkSoftPermissions(project.id!!, user)
        verify(projectRepository, times(1)).findById(project.id!!)
        verify(projectRepository, times(0)).save(project)
    }

    @Test
    fun shouldDeleteProject() {
        val project = createTestProject("test")
        val user = createTestUser()
        given(memberService.checkHardPermissions(project.id!!, user)).willReturn(Mono.just(project.id!!))
        given(projectRepository.existsById(project.id!!)).willReturn(Mono.just(true))
        given(projectRepository.deleteById(project.id!!)).willReturn(Mono.empty())
        val projectId = projectService.deleteProject(project.id!!, user).block()
        assertThat(projectId == project.id)
        verify(projectDeletedSagaService, times(1)).startSaga(project.id!!)
    }

}
