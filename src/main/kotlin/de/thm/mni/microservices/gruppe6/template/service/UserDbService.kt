package de.thm.mni.microservices.gruppe6.template.service

import de.thm.mni.microservices.gruppe6.template.model.persistence.User
import de.thm.mni.microservices.gruppe6.template.model.message.UserDTO
import de.thm.mni.microservices.gruppe6.template.model.persistence.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class UserDbService(@Autowired val userRepo: UserRepository) {

    private var nextId: Long = 0

    fun getAllUsers(): Flux<User> = userRepo.findAll()

    fun putUser(userDTO: UserDTO): Mono<User> {
        return userRepo.save(User(nextId++, userDTO))
    }

    fun updateUser(id: Long, userDTO: UserDTO): Mono<User> {
        val user = userRepo.findById(id)
        return user.map { it.applyUserDTO(userDTO) }
    }

    fun deleteUser(id: Long): Mono<Void> {
        return userRepo.deleteById(id)
    }

    fun User.applyUserDTO(userDTO: UserDTO): User {
        this.username = userDTO.username!!
        this.lastName = userDTO.lastName!!
        this.name = userDTO.name!!
        this.dateOfBirth = userDTO.dateOfBirth!!
        this.globalRole = userDTO.globalRole!!
        return this
    }

}
