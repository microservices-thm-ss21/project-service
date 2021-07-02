package de.thm.mni.microservices.gruppe6.project.service


import de.thm.mni.microservices.gruppe6.lib.event.DataEventCode.*
import de.thm.mni.microservices.gruppe6.lib.event.UserDataEvent
import de.thm.mni.microservices.gruppe6.project.model.persistence.User
import de.thm.mni.microservices.gruppe6.project.model.persistence.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Component
class UserDbService(@Autowired private val userRepo: UserRepository) {

    /**
     * returns all users
     */
    fun getAllUsers(): Flux<User> = userRepo.findAll()

    /**
     * returns stored user
     * @param id: user id
     */
    fun getUserById(id: UUID): Mono<User> = userRepo.findById(id)

    /**
     * create new user
     */
    fun createProjectWithMembers(user: User): Mono<User> {
        return userRepo.save(user)
    }

    /**
     * Deletes user by id
     * @param id: user id
     */
    fun deleteUser(id: UUID): Mono<Void> {
        return userRepo.deleteById(id)
    }

    fun receiveUpdate(userDataEvent: UserDataEvent) {
        when (userDataEvent.code) {
            CREATED -> userRepo.saveUser(userDataEvent.id).subscribe()
            DELETED -> userRepo.deleteById(userDataEvent.id).subscribe()
            UPDATED -> {
            }
            else -> throw IllegalArgumentException("Unexpected code for userEvent: ${userDataEvent.code}")
        }
    }
}
