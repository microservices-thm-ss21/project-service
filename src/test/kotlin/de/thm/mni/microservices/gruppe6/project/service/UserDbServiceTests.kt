package de.thm.mni.microservices.gruppe6.project.service

import de.thm.mni.microservices.gruppe6.lib.classes.projectService.Project
import de.thm.mni.microservices.gruppe6.lib.classes.userService.UserId
import de.thm.mni.microservices.gruppe6.project.model.persistence.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@ExtendWith(MockitoExtension::class)
class UserDbServiceTests(
    @Mock private val userRepository: UserRepository
) {

    private val userService = UserDbService(userRepository)

    @Test
    fun testShouldReturnEmptyListOfUsers() {
        given(userRepository.findAll()).willReturn(Flux.fromIterable(emptyList()))
        val projects: List<UserId>? = userService.getAllUsers().collectList().block()

        assertThat(projects).isNotNull
        assertThat(projects).isEmpty()
        assertThat(projects).isEqualTo(emptyList<Project>())

        verify(userRepository, times(1)).findAll()
    }

    @Test
    fun testShouldReturnAllUsers() {
        val user1 = UserId(UUID.randomUUID())
        val user2 = UserId(UUID.randomUUID())
        val user3 = UserId(UUID.randomUUID())
        val userList = listOf(user1, user2, user3)

        given(userRepository.findAll()).willReturn(Flux.fromIterable(userList))
        val projects: List<UserId>? = userService.getAllUsers().collectList().block()

        assertThat(projects).`as`("list of users").isNotNull
        assertThat(projects).`as`("list of users").hasSize(userList.size)
        projects!!.withIndex()
            .forEach { assertThat(it.value).`as`("project " + it.index).isEqualTo(userList[it.index]) }

        Mockito.verify(userRepository, times(1)).findAll()
    }


    @Test
    fun shouldDeleteUser() {
        val id = UUID.randomUUID()

        given(userRepository.deleteById(id)).willReturn(Mono.empty())

        userService.deleteUser(id).block()

        verify(userRepository, times(1)).deleteById(id)
    }

}
