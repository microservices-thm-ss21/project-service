package de.thm.mni.microservices.gruppe6.project.controller

import de.thm.mni.microservices.gruppe6.project.model.persistence.Member
import de.thm.mni.microservices.gruppe6.project.service.MemberDbService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [MemberController::class])
class MemberControllerTests {

    @Autowired private lateinit var webTestClient: WebTestClient
    @MockBean private lateinit var memberService: MemberDbService

    fun getMembersURI(projectId: UUID): String {
        return "/api/projects/$projectId/members"
    }

    fun createTestMember(projectId: UUID, projectRole: String): Member {
        return Member(UUID.randomUUID(), projectId, UUID.randomUUID(), projectRole)
    }

    @Test
    fun testShouldGetEmptyListOfMembers() {
        val projectId = UUID.randomUUID()
        val members = emptyList<Member>()
        given(memberService.getMembers(projectId)).willReturn(Flux.fromIterable(members))

        webTestClient
                .get()
                .uri(getMembersURI(projectId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody().jsonPath("$", members)

        verify(memberService, times(1)).getMembers(projectId)
    }

    @Test
    fun testShouldGetMembers() {
        val projectId = UUID.randomUUID()
        val members = listOf(createTestMember(projectId, "admin"), createTestMember(projectId, "normal"), createTestMember(projectId, "normal"))
        given(memberService.getMembers(projectId)).willReturn(Flux.fromIterable(members))

        webTestClient
                .get()
                .uri(getMembersURI(projectId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody().jsonPath("$", members)

        verify(memberService, times(1)).getMembers(projectId)
    }

    @Test
    fun testShouldDeleteMembers() {
        val projectId = UUID.randomUUID()
        given(memberService.deleteAllMembers(projectId)).willReturn(Mono.empty())

        webTestClient
                .delete()
                .uri(getMembersURI(projectId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody().isEmpty

        verify(memberService, times(1)).deleteAllMembers(projectId)
    }

}