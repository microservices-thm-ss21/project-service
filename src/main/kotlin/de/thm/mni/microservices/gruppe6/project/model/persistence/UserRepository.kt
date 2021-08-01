package de.thm.mni.microservices.gruppe6.project.model.persistence

import de.thm.mni.microservices.gruppe6.lib.classes.userService.UserId
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.util.*

interface UserRepository : ReactiveCrudRepository<UserId, UUID> {

    @Query("INSERT INTO userIds VALUES (:userId)")
    fun saveUser(userId: UUID) : Mono<Void>
}
