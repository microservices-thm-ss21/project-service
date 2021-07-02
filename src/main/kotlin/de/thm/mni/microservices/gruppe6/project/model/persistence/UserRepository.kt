package de.thm.mni.microservices.gruppe6.project.model.persistence

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.util.*

interface UserRepository : ReactiveCrudRepository<User, UUID> {

    @Query("INSERT INTO users VALUES (:userId)")
    fun saveUser(userId: UUID) : Mono<Void>
}
