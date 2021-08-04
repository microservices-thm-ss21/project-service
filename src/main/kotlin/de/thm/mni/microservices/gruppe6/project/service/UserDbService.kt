package de.thm.mni.microservices.gruppe6.project.service


import de.thm.mni.microservices.gruppe6.lib.classes.userService.UserId
import de.thm.mni.microservices.gruppe6.lib.event.DataEventCode.*
import de.thm.mni.microservices.gruppe6.lib.event.UserDataEvent
import de.thm.mni.microservices.gruppe6.project.model.persistence.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * Implements the functionality used to process users
 */
@Component
class UserDbService(@Autowired private val userRepo: UserRepository) {

    /**
     * Returns all userIds
     * @return flux of userIds
     */
    fun getAllUsers(): Flux<UserId> = userRepo.findAll()

    /**
     * Deletes a userId
     * @param userId: user id
     */
    fun deleteUser(userId: UUID): Mono<Void> {
        return userRepo.deleteById(userId)
    }

    /**
     * Handles all the incoming UserDataEvents
     * @param userDataEvent
     */
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
