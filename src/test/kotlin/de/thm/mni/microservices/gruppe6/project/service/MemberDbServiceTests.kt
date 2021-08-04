package de.thm.mni.microservices.gruppe6.project.service

import de.thm.mni.microservices.gruppe6.lib.classes.projectService.*
import de.thm.mni.microservices.gruppe6.lib.classes.userService.User
import de.thm.mni.microservices.gruppe6.lib.exception.ServiceException
import de.thm.mni.microservices.gruppe6.project.model.persistence.MemberRepository
import de.thm.mni.microservices.gruppe6.project.model.persistence.ProjectRepository
import de.thm.mni.microservices.gruppe6.project.model.persistence.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.*
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.jms.core.JmsTemplate
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime

import java.util.*

@ExtendWith(MockitoExtension::class)
class MemberDbServiceTests(
        @Mock private val userRepo: UserRepository,
        @Mock private val projectRepo: ProjectRepository,
        @Mock private val memberRepo: MemberRepository,
        @Mock private val sender: JmsTemplate
) {
    private val memberService = MemberDbService(userRepo, projectRepo, memberRepo, sender)

    private fun createTestMember(projectId: UUID, projectRole: String, userId: UUID): Member {
        return Member(UUID.randomUUID(), projectId, userId, projectRole)
    }

    private fun createTestUser(): User {
        return User(UUID.randomUUID(), "username", "Password", "name", "lastName", "email",
                LocalDate.now(), LocalDateTime.now(), "ADMIN", LocalDateTime.now())
    }

    @Test
    fun testShouldReturnEmptyListOfMembers() {
        val projectId = UUID.randomUUID()
        given(memberRepo.getMembersByProjectID(projectId)).willReturn(Flux.fromIterable(emptyList()))
        val members: List<Member>? = memberService.getMembers(projectId).collectList().block()

        assertThat(members).isNotNull
        assertThat(members).isEmpty()
        assertThat(members).isEqualTo(emptyList<Project>())

        verify(memberRepo, times(1)).getMembersByProjectID(projectId)
    }


    @Test
    fun testShouldReturnAllMembers() {
        val projectId = UUID.randomUUID()
        val member1 = createTestMember(projectId, "admin", UUID.randomUUID())
        val member2 = createTestMember(projectId, "normal", UUID.randomUUID())
        val member3 = createTestMember(projectId, "normal", UUID.randomUUID())
        val memberList = listOf(member1, member2, member3)

        given(memberRepo.getMembersByProjectID(projectId)).willReturn(Flux.fromIterable(memberList))
        val members: List<Member>? = memberService.getMembers(projectId).collectList().block()

        assertThat(members).`as`("list of members of project with id $projectId").isNotNull
        assertThat(members).`as`("list of members of project with id $projectId").hasSize(memberList.size)
        members!!.withIndex().forEach { assertThat(it.value).`as`("member $it.index").isEqualTo(memberList[it.index]) }

        verify(memberRepo, times(1)).getMembersByProjectID(projectId)
    }

    @Test
    fun testShouldAddMember1() {
        // Member already in Project
        val service = spy(memberService)
        val projectId = UUID.randomUUID()
        val user = createTestUser()
        val member = createTestMember(projectId, ProjectRole.ADMIN.name, UUID.randomUUID())
        doReturn(Mono.just(true)).`when`(service).isMember(projectId, member.userId)
        given(memberRepo.findMemberOfProject(projectId, member.userId)).willReturn(Mono.just(member))

        val returnedMember = service.addMember(projectId, user, member.userId, ProjectRole.valueOf(member.projectRole)).block()

        assertThat(returnedMember).`as`("list of created members of project with id $projectId").isNotNull
        assertThat(returnedMember).`as`("returnedMember").isEqualTo(member)
    }

    @Test
    fun testShouldAddMember2() {
        // Member not in Project
        val projectId = UUID.randomUUID()
        val user = createTestUser()
        val member = createTestMember(projectId, ProjectRole.ADMIN.name, UUID.randomUUID())
        val service = spy(memberService)
        doReturn(Mono.just(false)).`when`(service).isMember(projectId, member.userId)
        doReturn(Mono.just(member)).`when`(service).addNewMember(projectId, user, member.userId, ProjectRole.valueOf(member.projectRole))

        val returnedMember = service.addMember(projectId, user, member.userId, ProjectRole.valueOf(member.projectRole)).block()

        assertThat(returnedMember).`as`("list of created members of project with id $projectId").isNotNull
        assertThat(returnedMember).`as`("returnedMember").isEqualTo(member)
    }

    @Test
    fun testShouldNotAddMember() {
        // Member with different role in project
        val projectId = UUID.randomUUID()
        val user = createTestUser()
        val member = createTestMember(projectId, ProjectRole.ADMIN.name, UUID.randomUUID())
        val memberOld = member.copy()
        val service = spy(memberService)
        memberOld.projectRole = ProjectRole.USER.name
        doReturn(Mono.just(true)).`when`(service).isMember(projectId, member.userId)
        given(memberRepo.findMemberOfProject(projectId, member.userId)).willReturn(Mono.just(memberOld))

        var error: Throwable? = null
        try{
            service.addMember(projectId, user, member.userId, ProjectRole.valueOf(member.projectRole)).block()
        } catch (e: Throwable) {
            error = e
        }
        assertThat(error != null)
        assertThat(error is ServiceException)
        assertThat((error as ServiceException).status.value() == HttpStatus.NOT_FOUND.value())
    }

    @Test
    fun shouldDeleteMember() {
        val service = spy(memberService)
        val projectId = UUID.randomUUID()
        val user = createTestUser()
        val member = createTestMember(projectId, ProjectRole.ADMIN.name, UUID.randomUUID())

        doReturn(Mono.just(projectId)).`when`(service).checkHardPermissions(projectId, user)
        doReturn(Mono.just(true)).`when`(service).isMember(projectId, member.userId)
        given(memberRepo.deleteByUserIdAndProjectId(member.userId, projectId)).willReturn(Mono.empty())

        val deletedMemberId = service.deleteMember(projectId, user, member.userId).block()

        assertThat(deletedMemberId).isEqualTo(member.userId)

        verify(service, times(1)).checkHardPermissions(projectId, user)
        verify(service, times(1)).isMember(projectId, member.userId)
        verify(memberRepo, times(1)).deleteByUserIdAndProjectId(member.userId, projectId)
    }

    @Test
    fun shouldNotDeleteMember1() {
        val service = spy(memberService)
        val projectId = UUID.randomUUID()
        val user = createTestUser()
        val member = createTestMember(projectId, ProjectRole.ADMIN.name, UUID.randomUUID())

        doThrow(ServiceException(HttpStatus.FORBIDDEN)).`when`(service).checkHardPermissions(projectId, user)
        given(memberRepo.deleteByUserIdAndProjectId(member.userId, projectId)).willReturn(Mono.empty())

        var error: Throwable? = null
        try {
            service.deleteMember(projectId, user, member.userId).block()
        } catch (e: Throwable) {
            error = e
        }
        assertThat(error != null)
        assertThat(error is ServiceException)
        assertThat((error as ServiceException).status.value() == HttpStatus.FORBIDDEN.value())

        verify(service, times(1)).checkHardPermissions(projectId, user)
        verify(service, times(0)).isMember(projectId, member.userId)
        verify(memberRepo, times(0)).deleteByUserIdAndProjectId(member.userId, projectId)
    }

    @Test
    fun shouldNotDeleteMember2() {
        val service = spy(memberService)
        val projectId = UUID.randomUUID()
        val user = createTestUser()
        val member = createTestMember(projectId, ProjectRole.ADMIN.name, UUID.randomUUID())

        doReturn(Mono.just(projectId)).`when`(service).checkHardPermissions(projectId, user)
        doReturn(Mono.just(false)).`when`(service).isMember(projectId, member.userId)
        given(memberRepo.deleteByUserIdAndProjectId(member.userId, projectId)).willReturn(Mono.empty())

        var error: Throwable? = null
        try {
            service.deleteMember(projectId, user, member.userId).block()
        } catch (e: Throwable) {
            error = e
        }
        assertThat(error != null)
        assertThat(error is ServiceException)
        assertThat((error as ServiceException).status.value() == HttpStatus.NOT_FOUND.value())

        verify(service, times(1)).checkHardPermissions(projectId, user)
        verify(service, times(1)).isMember(projectId, member.userId)
        verify(memberRepo, times(0)).deleteByUserIdAndProjectId(member.userId, projectId)
    }

}


