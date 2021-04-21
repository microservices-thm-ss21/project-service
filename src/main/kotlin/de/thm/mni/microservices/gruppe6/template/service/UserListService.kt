package de.thm.mni.microservices.gruppe6.template.service

import de.thm.mni.microservices.gruppe6.template.model.persistence.User
import de.thm.mni.microservices.gruppe6.template.model.message.UserDTO
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Deprecated("Dummy implementation before database connection was configured.")
@Component
class UserListService {

    private val userList: MutableList<User> = ArrayList()
    private var nextId: Long = 0

    fun getAllUsers(): Flux<User> = Flux.fromIterable(userList)

    fun putUser(userDTO: UserDTO): Long {
        userList.add(User(nextId, userDTO))
        return nextId++
    }

    fun updateUser(id: Long, userDTO: UserDTO): User {
        val user = userList.find { it.id == id}!!
        user.username = userDTO.username!!
        user.lastName = userDTO.lastName!!
        user.name = userDTO.name!!
        user.dateOfBirth = userDTO.dateOfBirth!!
        user.globalRole = userDTO.globalRole!!
        return user
    }

    fun deleteUser(id: Long): Boolean {
        return userList.removeIf { it.id == id }
    }

}
